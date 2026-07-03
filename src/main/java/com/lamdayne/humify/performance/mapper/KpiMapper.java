package com.lamdayne.humify.performance.mapper;

import com.lamdayne.humify.performance.dto.response.KpiResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KpiMapper {
    @Mapping(source = "employee.id", target = "employeeId")
    KpiResponse toResponse(Kpi kpi);
}