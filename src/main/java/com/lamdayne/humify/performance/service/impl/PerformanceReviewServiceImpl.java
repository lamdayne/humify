package com.lamdayne.humify.performance.service.impl;


import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.performance.dto.request.CreatePerformanceReviewRequest;
import com.lamdayne.humify.performance.dto.request.ManagerReviewRequest;
import com.lamdayne.humify.performance.dto.request.SelfReviewRequest;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewSummaryResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.entity.KpiTemplate;
import com.lamdayne.humify.performance.entity.KpiTemplateItem;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import com.lamdayne.humify.performance.enums.KpiStatus;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.performance.mapper.PerformanceReviewMapper;
import com.lamdayne.humify.performance.repository.KpiTemplateRepository;
import com.lamdayne.humify.performance.repository.PerformanceReviewRepository;
import com.lamdayne.humify.performance.service.KpiCalculationService;
import com.lamdayne.humify.performance.service.PerformanceReviewService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceReviewServiceImpl
        implements PerformanceReviewService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final KpiTemplateRepository kpiTemplateRepository;

    private final EmployeeService employeeService;
    private final UserService userService;
    private final CompanyService companyService;
    private final KpiCalculationService kpiCalculationService;
    private final PerformanceReviewMapper performanceReviewMapper;

    @Override
    @Transactional
    public PerformanceReviewResponse createReview(
            UserPrincipal userPrincipal,
            CreatePerformanceReviewRequest request
    ) {
        Long companyId = userPrincipal.getCompanyId();

        validatePeriod(
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        Company company =companyService.getCompanyById(companyId);

        Employee employee =
                employeeService.getEmployeeEntityByIdAndCompanyId(
                        request.getEmployeeId(),
                        companyId
                );

        User reviewer = userService.getUserEntityByIdAndCompanyId(

                request.getReviewerId(),
                companyId
        );
        if (!userService.canBePerformanceReviewer(reviewer.getId())) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_REVIEWER_INVALID
            );
        }
        KpiTemplate template = getTemplateOrThrow(
                companyId,
                request.getTemplateId()
        );

        validateTemplate(template);

        validateDuplicateReview(
                companyId,
                employee.getId(),
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        PerformanceReview review =
                new PerformanceReview();

        review.setCompany(company);
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setTemplate(template);

        review.setPeriodStart(request.getPeriodStart());
        review.setPeriodEnd(request.getPeriodEnd());

        review.setStatus(
                PerformanceReviewStatus.DRAFT
        );

        review.setSelfScore(null);
        review.setReviewerScore(null);
        review.setFinalScore(null);
        review.setFeedback(null);

        copyTemplateItemsToKpis(
                review,
                company,
                employee,
                template,
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        PerformanceReview saved =
                performanceReviewRepository.save(review);

        return performanceReviewMapper.toResponse(saved);
    }

    @Override
    public PerformanceReviewResponse getReviewById(
            UserPrincipal userPrincipal,
            Long reviewId
    ) {
        PerformanceReview review =
                getReviewOrThrow(
                        userPrincipal.getCompanyId(),
                        reviewId
                );

        return performanceReviewMapper.toResponse(review);
    }

    @Override
    public PageResponse<PerformanceReviewResponse> getReviews(
            UserPrincipal userPrincipal,
            Long employeeId,
            PerformanceReviewStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            int page,
            int size,
            String... sorts
    ) {
        if (periodStart != null
                && periodEnd != null
                && periodStart.isAfter(periodEnd)) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_PERIOD_INVALID
            );
        }

        Pageable pageable =
                PageableUtil.buildPageable(
                        page,
                        size,
                        sorts
                );

        Specification<PerformanceReview> spec =
                (root, query, cb) ->
                        cb.equal(
                                root.get("company").get("id"),
                                userPrincipal.getCompanyId()
                        );

        spec = spec.and(
                (root, query, cb) ->
                        cb.isNull(root.get("deletedAt"))
        );

        if (employeeId != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("employee").get("id"),
                                    employeeId
                            )
            );
        }

        if (status != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (periodStart != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("periodEnd"),
                                    periodStart
                            )
            );
        }

        if (periodEnd != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("periodStart"),
                                    periodEnd
                            )
            );
        }

        Page<PerformanceReview> reviewPage =
                performanceReviewRepository.findAll(
                        spec,
                        pageable
                );

        List<PerformanceReviewResponse> reviews =
                reviewPage.stream()
                        .map(performanceReviewMapper::toResponse)
                        .toList();

        return PageResponse.<PerformanceReviewResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .items(reviews)
                .build();
    }
    @Override
    public PageResponse<PerformanceReviewResponse> getMyReviews(
            UserPrincipal userPrincipal,
            PerformanceReviewStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            int page,
            int size,
            String... sorts
    ) {

        if (periodStart != null
                && periodEnd != null
                && periodStart.isAfter(periodEnd)) {

            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_PERIOD_INVALID
            );
        }

        User user =
                userService.getUserEntityByIdAndCompanyId(
                        userPrincipal.getId(),
                        userPrincipal.getCompanyId()
                );

        if (user.getEmployee() == null) {
            throw new AppException(
                    ErrorCode.EMPLOYEE_NOT_FOUND
            );
        }

        Long employeeId = user.getEmployee().getId();

        Pageable pageable =
                PageableUtil.buildPageable(
                        page,
                        size,
                        sorts
                );

        Specification<PerformanceReview> spec =
                (root, query, cb) ->
                        cb.and(
                                cb.equal(
                                        root.get("company").get("id"),
                                        userPrincipal.getCompanyId()
                                ),
                                cb.equal(
                                        root.get("employee").get("id"),
                                        employeeId
                                ),
                                cb.isNull(
                                        root.get("deletedAt")
                                )
                        );

        if (status != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (periodStart != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("periodEnd"),
                                    periodStart
                            )
            );
        }

        if (periodEnd != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("periodStart"),
                                    periodEnd
                            )
            );
        }

        Page<PerformanceReview> reviewPage =
                performanceReviewRepository.findAll(
                        spec,
                        pageable
                );

        List<PerformanceReviewResponse> reviews =
                reviewPage.stream()
                        .map(performanceReviewMapper::toResponse)
                        .toList();

        return PageResponse.<PerformanceReviewResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .items(reviews)
                .build();
    }

    //  review of manager/hr
    @Override
    public PageResponse<PerformanceReviewResponse> getMyAssignedReviews(
            UserPrincipal userPrincipal,
            Long employeeId,
            PerformanceReviewStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            int page,
            int size,
            String... sorts
    ) {

        if (periodStart != null
                && periodEnd != null
                && periodStart.isAfter(periodEnd)) {

            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_PERIOD_INVALID
            );
        }

        Long companyId = userPrincipal.getCompanyId();
        Long reviewerId = userPrincipal.getId();

        Pageable pageable =
                PageableUtil.buildPageable(
                        page,
                        size,
                        sorts
                );

        Specification<PerformanceReview> spec =
                (root, query, cb) ->
                        cb.and(
                                cb.equal(
                                        root.get("company").get("id"),
                                        companyId
                                ),
                                cb.equal(
                                        root.get("reviewer").get("id"),
                                        reviewerId
                                ),
                                cb.isNull(
                                        root.get("deletedAt")
                                )
                        );

        if (employeeId != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("employee").get("id"),
                                    employeeId
                            )
            );
        }

        if (status != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        if (periodStart != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("periodEnd"),
                                    periodStart
                            )
            );
        }

        if (periodEnd != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.lessThanOrEqualTo(
                                    root.get("periodStart"),
                                    periodEnd
                            )
            );
        }

        Page<PerformanceReview> reviewPage =
                performanceReviewRepository.findAll(
                        spec,
                        pageable
                );

        List<PerformanceReviewResponse> reviews =
                reviewPage.stream()
                        .map(performanceReviewMapper::toResponse)
                        .toList();

        return PageResponse.<PerformanceReviewResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .items(reviews)
                .build();
    }

    private void copyTemplateItemsToKpis(
            PerformanceReview review,
            Company company,
            Employee employee,
            KpiTemplate template,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        for (KpiTemplateItem item : template.getItems()) {

            if (item.getDeletedAt() != null) {
                continue;
            }

            Kpi kpi = Kpi.builder()
                    .company(company)
                    .employee(employee)

                    .title(item.getTitle())
                    .description(item.getDescription())

                    .metricType(item.getMetricType())

                    .targetValue(item.getTargetValue())
                    .currentValue(0.0)

                    .unit(item.getUnit())
                    .weight(item.getWeight())

                    .score(0.0)

                    .startDate(periodStart)
                    .endDate(periodEnd)

                    .status(KpiStatus.IN_PROGRESS)

                    .build();

            review.addKpi(kpi);
        }
    }

    private void validatePeriod(
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        if (periodStart == null || periodEnd == null) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_PERIOD_REQUIRED
            );
        }

        if (periodStart.isAfter(periodEnd)) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_PERIOD_INVALID
            );
        }
    }
    private void validateTemplate(
            KpiTemplate template
    ) {
        if (!Boolean.TRUE.equals(template.getIsActive())) {
            throw new AppException(
                    ErrorCode.KPI_TEMPLATE_INACTIVE
            );
        }

        boolean hasActiveItems = template.getItems()
                .stream()
                .anyMatch(item ->
                        item.getDeletedAt() == null
                );

        if (!hasActiveItems) {
            throw new AppException(
                    ErrorCode.KPI_TEMPLATE_ITEMS_EMPTY
            );
        }
    }

    private void validateDuplicateReview(
            Long companyId,
            Long employeeId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        boolean exists =
                performanceReviewRepository
                        .existsByCompany_IdAndEmployee_IdAndPeriodStartAndPeriodEndAndDeletedAtIsNull(
                                companyId,
                                employeeId,
                                periodStart,
                                periodEnd
                        );

        if (exists) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_EXISTED
            );
        }
    }

    private PerformanceReview getReviewOrThrow(
            Long companyId,
            Long reviewId
    ) {
        return performanceReviewRepository
                .findByIdAndCompany_IdAndDeletedAtIsNull(
                        reviewId,
                        companyId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.PERFORMANCE_REVIEW_NOT_FOUND
                        )
                );
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

    @Override
    @Transactional
    public PerformanceReviewResponse selfReview(
            UserPrincipal userPrincipal,
            Long reviewId,
            SelfReviewRequest request
    ) {

        PerformanceReview review =
                getReviewOrThrow(
                        userPrincipal.getCompanyId(),
                        reviewId
                );

        if (review.getStatus()
                != PerformanceReviewStatus.DRAFT) {

            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_INVALID_STATUS
            );
        }

        User currentUser =
                userService.getUserEntityByIdAndCompanyId(
                        userPrincipal.getId(),
                        userPrincipal.getCompanyId()
                );

        if (currentUser.getEmployee() == null
                || !currentUser.getEmployee()
                .getId()
                .equals(
                        review.getEmployee().getId()
                )) {

            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_SELF_REVIEW_FORBIDDEN
            );
        }

        review.setSelfScore(
                request.getSelfScore()
        );

        review.setStatus(
                PerformanceReviewStatus.SELF_REVIEW
        );

        return performanceReviewMapper.toResponse(
                performanceReviewRepository.save(review)
        );
    }


    @Override
    @Transactional
    public PerformanceReviewResponse managerReview(
            UserPrincipal userPrincipal,
            Long reviewId,
            ManagerReviewRequest request
    ) {
        PerformanceReview review = getReviewOrThrow(
                userPrincipal.getCompanyId(),
                reviewId
        );

        if (review.getStatus() != PerformanceReviewStatus.SELF_REVIEW) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_INVALID_STATUS
            );
        }
        if (!review.getReviewer()
                .getId()
                .equals(userPrincipal.getId())) {

            boolean fullAccess =
                    userPrincipal.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority()
                                            .equals("FULL_ACCESS")
                            );

            if (!fullAccess) {
                throw new AppException(
                        ErrorCode.PERFORMANCE_REVIEW_REVIEWER_FORBIDDEN
                );
            }
        }
        review.setReviewerScore(request.getReviewerScore());
        review.setFeedback(request.getFeedback());

        review.setStatus(
                PerformanceReviewStatus.MANAGER_REVIEW
        );

        PerformanceReview saved =
                performanceReviewRepository.save(review);

        return performanceReviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PerformanceReviewResponse completeReview(
            UserPrincipal userPrincipal,
            Long reviewId
    ) {

        PerformanceReview review =
                getReviewOrThrow(
                        userPrincipal.getCompanyId(),
                        reviewId
                );

        if (review.getStatus()
                != PerformanceReviewStatus.MANAGER_REVIEW) {

            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_INVALID_STATUS
            );
        }

        // Chỉ reviewer được gán hoặc FULL_ACCESS mới được complete
        if (!review.getReviewer()
                .getId()
                .equals(userPrincipal.getId())) {

            boolean fullAccess =
                    userPrincipal.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority.getAuthority()
                                            .equals("FULL_ACCESS")
                            );

            if (!fullAccess) {
                throw new AppException(
                        ErrorCode.PERFORMANCE_REVIEW_REVIEWER_FORBIDDEN
                );
            }
        }

        // Tính KPI lần cuối
        kpiCalculationService.calculateReviewKpis(review);

        // Chốt trạng thái KPI
        for (Kpi kpi : review.getKpis()) {

            if (kpi.getDeletedAt() != null) {
                continue;
            }

            if (kpi.getCurrentValue() >= kpi.getTargetValue()) {
                kpi.setStatus(KpiStatus.ACHIEVED);
            } else {
                kpi.setStatus(KpiStatus.FAILED);
            }
        }

        // Lock review
        review.setStatus(
                PerformanceReviewStatus.COMPLETED
        );

        PerformanceReview saved =
                performanceReviewRepository.save(review);

        return performanceReviewMapper.toResponse(saved);
    }

    @Override
    public PerformanceReviewSummaryResponse getMyAssignedReviewSummary(
            UserPrincipal userPrincipal
    ) {

        Long companyId =
                userPrincipal.getCompanyId();

        Long reviewerId =
                userPrincipal.getId();

        long totalReviews =
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndDeletedAtIsNull(
                                companyId,
                                reviewerId
                        );

        long awaitingSelfReview =
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                companyId,
                                reviewerId,
                                PerformanceReviewStatus.DRAFT
                        );

        long awaitingManagerReview =
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                companyId,
                                reviewerId,
                                PerformanceReviewStatus.SELF_REVIEW
                        );

        long awaitingCompletion =
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                companyId,
                                reviewerId,
                                PerformanceReviewStatus.MANAGER_REVIEW
                        );

        long completed =
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                companyId,
                                reviewerId,
                                PerformanceReviewStatus.COMPLETED
                        );

        return PerformanceReviewSummaryResponse.builder()
                .totalReviews(totalReviews)
                .awaitingSelfReview(awaitingSelfReview)
                .awaitingManagerReview(awaitingManagerReview)
                .awaitingCompletion(awaitingCompletion)
                .completed(completed)
                .build();
    }
}