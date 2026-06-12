package com.lamdayne.humify.position.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.position.dto.request.CreatePositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;

public interface PositionService {

    PositionResponse createPosition(UserPrincipal userPrincipal, CreatePositionRequest request);

    PageResponse<PositionResponse> getAllPositions(int page, int size, String... sorts);
}