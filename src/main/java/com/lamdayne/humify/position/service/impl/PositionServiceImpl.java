package com.lamdayne.humify.position.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.position.dto.request.CreatePositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.mapper.PositionMapper;
import com.lamdayne.humify.position.repository.PositionRepository;
import com.lamdayne.humify.position.service.PositionAccessService;
import com.lamdayne.humify.position.service.PositionService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionServiceImpl implements PositionService, PositionAccessService {

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
    public PageResponse<PositionResponse> getAllPositions(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);

        Page<Position> positions = positionRepository.findAll(pageable);

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

    @Override
    public Position getReferenceById(Long id) {
        return positionRepository.getReferenceById(id);
    }

    @Override
    public Position getById(Long id) {
        return positionRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long id) {
        return positionRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndCompanyId(Long id, Long companyId) {
        return positionRepository.existsByIdAndCompanyId(id, companyId);
    }

    @Override
    public Optional<Position> findByName(String name) {
        return positionRepository.findByName(name);
    }

    @Override
    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    @Override
    @Transactional
    public PositionResponse updatePosition(Long id, CreatePositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        positionMapper.updatePosition(position, request);
        Position saved = positionRepository.save(position);
        return positionMapper.toPositionResponse(saved);
    }

    @Override
    @Transactional
    public void deletePosition(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new AppException(ErrorCode.POSITION_NOT_FOUND);
        }
        positionRepository.deleteById(id);
    }

    @Override
    public Position getPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
    }

    @Override
    public PositionResponse getPositionResponseById(Long id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        return positionMapper.toPositionResponse(position);
    }

    @Override
    public List<Position> getPositionsByDepartmentId(Long departmentId) {
        return positionRepository.findAll();
    }
}