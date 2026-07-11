package com.lamdayne.humify.payroll.dto.response;


import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PayrollPeriodResponse {

    private Long id;
    private Long companyId;
    private String name;
    private Integer month;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal standardWorkDays;
    private PayrollPeriodStatus status;
}
