package com.lamdayne.humify.position.mapper;

import com.lamdayne.humify.position.dto.request.PositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.entity.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PositionMapper {

    Position toPosition(PositionRequest request);

    @Mapping(source = "company.id", target = "companyId")
    PositionResponse toPositionResponse(Position position);

    @Mapping(source = "company.id", target = "companyId")
    List<PositionResponse> toPositionResponse(List<Position> positions);

}