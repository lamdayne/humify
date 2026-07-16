package com.lamdayne.humify.payroll.dto.response;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPayslipResponse {
    private Long id;
    private String payrollPeriodName;
    private Integer month;
    private Integer year;

    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal unpaidLeaveDays;

    private BigDecimal grossSalary;
    private BigDecimal totalDeductions; // = BHXH + BHYT + BHTN + thuế TNCN + khấu trừ khác
    private BigDecimal netSalary;

    private PayslipStatus status;
    private LocalDate paymentDate;


}
