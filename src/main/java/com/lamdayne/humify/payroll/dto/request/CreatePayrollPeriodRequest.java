package com.lamdayne.humify.payroll.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreatePayrollPeriodRequest {

    @NotBlank(message = "PAYROLL_NAME_REQUIRED")
    private String name;

    @NotNull(message = "MONTH_REQUIRED")
    @Min(value = 1, message = "MONTH_INVALID")
    @Max(value = 12, message = "MONTH_INVALID")
    private Integer month;

    @NotNull(message = "YEAR_REQUIRED")
    private Integer year;

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "END_DATE_REQUIRED")
    private LocalDate endDate;

    @NotNull(message = "STANDARD_WORK_DAYS_REQUIRED")
    private BigDecimal standardWorkDays;
}
