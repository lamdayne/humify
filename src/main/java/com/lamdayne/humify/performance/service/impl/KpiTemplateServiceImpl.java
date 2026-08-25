package com.lamdayne.humify.performance.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.performance.dto.request.CreateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.request.KpiTemplateItemRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.response.KpiTemplateResponse;
import com.lamdayne.humify.performance.entity.KpiTemplate;
import com.lamdayne.humify.performance.entity.KpiTemplateItem;
import com.lamdayne.humify.performance.mapper.KpiTemplateMapper;
import com.lamdayne.humify.performance.repository.KpiTemplateRepository;
import com.lamdayne.humify.performance.service.KpiTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lamdayne.humify.common.exception.ErrorCode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiTemplateServiceImpl implements KpiTemplateService {

    private final KpiTemplateRepository kpiTemplateRepository;
    private final KpiTemplateMapper kpiTemplateMapper;

    @Override
    @Transactional
    public KpiTemplateResponse createTemplate(
            UserPrincipal userPrincipal,
            CreateKpiTemplateRequest request
    ) {
        Long companyId = userPrincipal.getCompanyId();
        Long userId = userPrincipal.getId();

        validateWeight(request.getItems());

        validateTemplateName(
                companyId,
                request.getName(),
                null
        );

        KpiTemplate template = new KpiTemplate();

        template.setCompanyId(companyId);
        template.setCreatedBy(userId);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setIsActive(true);

        addItems(
                template,
                request.getItems()
        );

        KpiTemplate savedTemplate =
                kpiTemplateRepository.save(template);

        return kpiTemplateMapper.toResponse(savedTemplate);
    }

    @Override
    public List<KpiTemplateResponse> getAllTemplates(
            UserPrincipal userPrincipal
    ) {
        Long companyId = userPrincipal.getCompanyId();

        return kpiTemplateRepository
                .findAllByCompanyIdAndDeletedAtIsNull(companyId)
                .stream()
                .map(kpiTemplateMapper::toResponse)
                .toList();
    }

    @Override
    public KpiTemplateResponse getTemplateById(
            UserPrincipal userPrincipal,
            Long templateId
    ) {
        Long companyId = userPrincipal.getCompanyId();

        KpiTemplate template =
                getTemplateOrThrow(
                        companyId,
                        templateId
                );

        return kpiTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional
    public KpiTemplateResponse updateTemplate(
            UserPrincipal userPrincipal,
            Long templateId,
            UpdateKpiTemplateRequest request
    ) {
        Long companyId = userPrincipal.getCompanyId();

        KpiTemplate template =
                getTemplateOrThrow(
                        companyId,
                        templateId
                );

        validateTemplateName(
                companyId,
                request.getName(),
                templateId
        );

        validateWeight(request.getItems());

        template.setName(request.getName());
        template.setDescription(request.getDescription());

        if (request.getIsActive() != null) {
            template.setIsActive(request.getIsActive());
        }

        /*
         * Vì KpiTemplate có:
         *
         * cascade = CascadeType.ALL
         * orphanRemoval = true
         *
         * nên clear() sẽ xóa các item cũ khỏi DB.
         */
        template.getItems().clear();

        addItems(
                template,
                request.getItems()
        );

        KpiTemplate savedTemplate =
                kpiTemplateRepository.save(template);

        return kpiTemplateMapper.toResponse(savedTemplate);
    }

    @Override
    @Transactional
    public void deleteTemplate(
            UserPrincipal userPrincipal,
            Long templateId
    ) {
        Long companyId = userPrincipal.getCompanyId();

        KpiTemplate template =
                getTemplateOrThrow(
                        companyId,
                        templateId
                );

        template.setIsActive(false);
        template.setDeletedAt(Instant.now());

        kpiTemplateRepository.save(template);
    }

    private KpiTemplate getTemplateOrThrow(
            Long companyId,
            Long templateId
    ) {
        return kpiTemplateRepository
                .findByIdAndCompanyIdAndDeletedAtIsNull(
                        templateId,
                        companyId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.KPI_TEMPLATE_NOT_FOUND
                        )
                );
    }

    private void addItems(
            KpiTemplate template,
            List<KpiTemplateItemRequest> requests
    ) {
        for (KpiTemplateItemRequest request : requests) {

            KpiTemplateItem item =
                    new KpiTemplateItem();

            item.setTitle(request.getTitle());
            item.setDescription(request.getDescription());
            item.setMetricType(request.getMetricType());
            item.setTargetValue(request.getTargetValue());
            item.setUnit(request.getUnit());
            item.setWeight(request.getWeight());

            /*
             * Quan trọng:
             * addItem() sẽ set cả hai phía:
             *
             * template.items.add(item)
             * item.setTemplate(template)
             */
            template.addItem(item);
        }
    }

    private void validateWeight(
            List<KpiTemplateItemRequest> items
    ) {
        if (items == null || items.isEmpty()) {
            throw new AppException(
                    ErrorCode.KPI_TEMPLATE_ITEMS_EMPTY
            );
        }

        double totalWeight = items.stream()
                .mapToDouble(KpiTemplateItemRequest::getWeight)
                .sum();

        if (Math.abs(totalWeight - 100.0) > 0.001) {
            throw new AppException(
                    ErrorCode.KPI_TEMPLATE_WEIGHT_INVALID
            );

        }
    }

    private void validateTemplateName(
            Long companyId,
            String name,
            Long currentTemplateId
    ) {
        boolean exists;

        if (currentTemplateId == null) {

            exists =
                    kpiTemplateRepository
                            .existsByCompanyIdAndNameIgnoreCaseAndDeletedAtIsNull(
                                    companyId,
                                    name
                            );

        } else {

            exists =
                    kpiTemplateRepository
                            .existsByCompanyIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                                    companyId,
                                    name,
                                    currentTemplateId
                            );
        }

        if (exists) {
            throw new AppException(
                    ErrorCode.KPI_TEMPLATE_NAME_EXISTED
            );
        }
    }
}