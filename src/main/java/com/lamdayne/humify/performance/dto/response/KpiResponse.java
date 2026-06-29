package com.lamdayne.humify.performance.dto.response;

import com.lamdayne.humify.performance.enums.KpiStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class KpiResponse {
    private Long id;
    private Long employeeId;
    private String title;
    private String description;
    private Double targetValue;
    private Double currentValue;
    private String unit;
    private Double weight;
    private LocalDate startDate;
    private LocalDate endDate;
    private KpiStatus status;
    private Instant createdAt;
}