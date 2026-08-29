package com.lamdayne.humify.performance.dto.request;

import com.lamdayne.humify.performance.enums.KpiMetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KpiTemplateItemRequest {

    @NotBlank(message = "KPI title is required")
    private String title;

    private String description;

    @NotNull(message = "Metric type is required")
    private KpiMetricType metricType;

    @NotNull(message = "Target value is required")
    @Positive(message = "Target value must be greater than 0")
    private Double targetValue;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be greater than 0")
    private Double weight;}