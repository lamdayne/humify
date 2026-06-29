package com.lamdayne.humify.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateKpiProgressRequest {
    @NotNull(message = "KPI_CURRENT_VALUE_REQUIRED")
    private Double currentValue;
}