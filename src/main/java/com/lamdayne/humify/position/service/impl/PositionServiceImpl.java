package com.lamdayne.humify.position.service.impl;

import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.position.dto.request.PositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.entity.Position;
import com.lamdayne.humify.position.mapper.PositionMapper;
import com.lamdayne.humify.position.repository.PositionRepository;
import com.lamdayne.humify.position.service.PositionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;
    private final PositionRepository positionRepository;
    private final CompanyService companyService;

    @Override
    @Transactional
    public PositionResponse createPosition(PositionRequest request) {

        Company company =
                companyService.getCompanyById(request.getCompanyId());

        Position position =
                positionMapper.toPosition(request);

        position.setCompany(company);

        return positionMapper.toPositionResponse(
                positionRepository.save(position)
        );
    }

    @Override
    public List<PositionResponse> getPositionByCompanyId(Long companyId) {

        List<Position> positions =
                positionRepository.findByCompanyId(companyId);

        return positionMapper.toPositionResponse(positions);
    }
}