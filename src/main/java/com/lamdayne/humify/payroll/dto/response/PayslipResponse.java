package com.lamdayne.humify.payroll.dto.response;

import com.lamdayne.humify.payroll.enums.PayslipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayslipResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;

    private BigDecimal baseSalary;
    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal unpaidLeaveDays;

    private BigDecimal salaryByWorkDays;
    private BigDecimal totalAllowances;
    private BigDecimal bonusKpi;
    private BigDecimal bonusProject;
    private BigDecimal otherBonuses;
    private BigDecimal otHours;
    private BigDecimal otSalary;
    private BigDecimal grossSalary;

    private BigDecimal deductionSocialInsurance;
    private BigDecimal deductionHealthInsurance;
    private BigDecimal deductionUnemploymentInsurance;
    private BigDecimal personalIncomeTax;
    private BigDecimal otherDeductions;

    private BigDecimal netSalary;

    private PayslipStatus status;
    private LocalDate paymentDate;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;

}
