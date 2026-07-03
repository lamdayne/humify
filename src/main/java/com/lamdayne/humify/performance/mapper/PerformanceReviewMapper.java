package com.lamdayne.humify.performance.mapper;


import com.lamdayne.humify.performance.dto.response.ReviewResponse;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PerformanceReviewMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "feedBack", target = "feedback")
    @Mapping(target = "systemCalculatedMetrics", ignore = true)
    ReviewResponse toResponse(PerformanceReview review);
}
