package com.lamdayne.humify.position.service;

import com.lamdayne.humify.position.dto.request.PositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;

import java.util.List;

public interface PositionService {

    PositionResponse createPosition(PositionRequest request);

    List<PositionResponse> getPositionByCompanyId(Long companyId);

}