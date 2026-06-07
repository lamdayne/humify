package com.lamdayne.humify.position.mapper;

import com.lamdayne.humify.position.dto.request.CreatePositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.entity.Position;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    Position toPosition(CreatePositionRequest request);

    PositionResponse toPositionResponse(Position position);
}