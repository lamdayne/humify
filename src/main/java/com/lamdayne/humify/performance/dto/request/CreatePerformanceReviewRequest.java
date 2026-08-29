package com.lamdayne.humify.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreatePerformanceReviewRequest {

    @NotNull(message = "EMPLOYEE_ID_REQUIRED")
    private Long employeeId;

    @NotNull(message = "REVIEWER_ID_REQUIRED")
    private Long reviewerId;

    @NotNull(message = "KPI_TEMPLATE_ID_REQUIRED")
    private Long templateId;

    @NotNull(message = "PERIOD_START_REQUIRED")
    private LocalDate periodStart;

    @NotNull(message = "PERIOD_END_REQUIRED")
    private LocalDate periodEnd;
}