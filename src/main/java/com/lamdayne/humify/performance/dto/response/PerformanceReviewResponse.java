package com.lamdayne.humify.performance.dto.response;

import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
public class PerformanceReviewResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private Long reviewerId;
    private Long templateId;
    private String templateName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Double selfScore;
    private Double reviewerScore;
    private Double finalScore;
    private String feedback;
    private PerformanceReviewStatus status;
    private List<KpiResponse> kpis;
}
