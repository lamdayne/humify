package com.lamdayne.humify.performance.dto.response;


import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder(toBuilder = true)
public class ReviewResponse {
    private Long id;
    private Long employeeId;
    private Long reviewerId;
    private String reviewPeriod;
    private Double selfScore;
    private Double reviewerScore;
    private Double finalScore;
    private String feedback;
    private PerformanceReviewStatus status;
    private SystemCalculatedMetricsResponse systemCalculatedMetrics;
    private Instant createdAt;
}
