package com.lamdayne.humify.position.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.position.dto.request.CreatePositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.mapper.PositionMapper;
import com.lamdayne.humify.position.repository.PositionRepository;
import com.lamdayne.humify.position.service.PositionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final CompanyService companyService;
    private final PositionMapper positionMapper;

    @Override
    @Transactional
    public PositionResponse createPosition(UserPrincipal userPrincipal, CreatePositionRequest request) {
        Company company = companyService.getCompanyById(userPrincipal.getCompanyId());

        Position position = positionMapper.toPosition(request);
        position.setCompany(company);

        Position savedPosition = positionRepository.save(position);
        log.info("Created position: {} for company: {}", savedPosition.getName(), userPrincipal.getCompanyId());

        return positionMapper.toPositionResponse(savedPosition);
    }

    @Override
    public PageResponse<PositionResponse> getAllPositions(Long companyId, int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);

        Page<Position> positions = positionRepository.findByCompanyId(companyId, pageable);

        List<PositionResponse> positionResponses = positions.stream()
                .map(positionMapper::toPositionResponse)
                .toList();

        return PageResponse.<PositionResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(positions.getTotalPages())
                .totalElements(positions.getTotalElements())
                .items(positionResponses)
                .build();
    }
}