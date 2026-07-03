package com.lamdayne.humify.performance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CreateKpiRequest {
    @NotBlank(message = "KPI_TITLE_REQUIRED")
    private String title;

    private String description;

    @NotNull(message = "KPI_TARGET_VALUE_REQUIRED")
    @Positive(message = "KPI_TARGET_VALUE_INVALID")
    private Double targetValue;

    @NotBlank(message = "KPI_UNIT_REQUIRED")
    private String unit;

    @NotNull(message = "KPI_WEIGHT_REQUIRED")
    @DecimalMin(value = "0.01", message = "KPI_WEIGHT_INVALID")
    @DecimalMax(value = "1.0", message = "KPI_WEIGHT_INVALID")
    private Double weight;

    @NotNull(message = "KPI_START_DATE_REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "KPI_END_DATE_REQUIRED")
    private LocalDate endDate;
}