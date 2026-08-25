package com.lamdayne.humify.performance.mapper;

import com.lamdayne.humify.performance.dto.response.KpiResponse;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PerformanceReviewMapper {

    KpiResponse toKpiResponse(Kpi kpi);

    @Mapping(
            target = "employeeId",
            source = "employee.id"
    )
    @Mapping(
            target = "employeeName",
            source = "employee.fullName"
    )
    @Mapping(
            target = "reviewerId",
            source = "reviewer.id"
    )
    @Mapping(
            target = "templateId",
            source = "template.id"
    )
    @Mapping(
            target = "templateName",
            source = "template.name"
    )
    PerformanceReviewResponse toResponse(
            PerformanceReview review
    );
}