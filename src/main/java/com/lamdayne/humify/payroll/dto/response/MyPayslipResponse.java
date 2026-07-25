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

    // --- Thông tin kỳ lương ---
    private String payrollPeriodName;
    private Integer month;
    private Integer year;

    // --- Chấm công trong kỳ ---
    private BigDecimal standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal paidLeaveDays;
    private BigDecimal unpaidLeaveDays;

    // --- Thu nhập ---
    private BigDecimal baseSalary;
    private BigDecimal grossSalary;       // Tổng thu nhập trước khấu trừ

    // --- Khấu trừ: tách riêng vì đây là nghĩa vụ đóng góp của chính nhân viên ---
    private BigDecimal deductionSocialInsurance;      // BHXH
    private BigDecimal deductionHealthInsurance;       // BHYT
    private BigDecimal deductionUnemploymentInsurance; // BHTN
    private BigDecimal personalIncomeTax;               // Thuế TNCN
    private BigDecimal otherDeductions;                 // Khấu trừ khác (tạm ứng, phạt...)
    private BigDecimal totalDeductions;                 // Tổng cộng, tiện hiển thị nhanh

    // --- Thực nhận ---
    private BigDecimal netSalary;

    // --- Trạng thái & thanh toán ---
    private PayslipStatus status;
    private LocalDate paymentDate;

    //thưởng
    private BigDecimal salaryByWorkDays;
    private BigDecimal totalAllowances;
    private BigDecimal bonusKpi;
    private BigDecimal bonusProject;
    private BigDecimal otherBonuses;


}
