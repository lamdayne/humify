package com.lamdayne.humify.attendance.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryReportResponse {
    private Long employeeId;
    private String employeeName;
    private Double totalPresentDays;
    private BigDecimal totalWorkedHours;
    private Long totalLateCount;
    private Long totalLateMinutes;
    private Long totalEarlyCount;
    private BigDecimal totalOtHours;
    private Double totalLeaveDays;
    private Double totalAbsentDays;
}