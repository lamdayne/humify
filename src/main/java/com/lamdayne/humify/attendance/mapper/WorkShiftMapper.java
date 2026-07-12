package com.lamdayne.humify.attendance.mapper;

import com.lamdayne.humify.attendance.dto.request.CreateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.response.WorkShiftResponse;
import com.lamdayne.humify.attendance.entity.WorkShift;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface WorkShiftMapper {

    WorkShiftResponse toResponse(WorkShift entity);

    WorkShift toEntity(CreateWorkShiftRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget WorkShift entity, UpdateWorkShiftRequest request);
}