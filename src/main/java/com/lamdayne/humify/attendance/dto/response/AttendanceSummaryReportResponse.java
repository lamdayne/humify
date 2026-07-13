package com.lamdayne.humify.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
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

    public AttendanceSummaryReportResponse(
            Long employeeId,
            String employeeName,
            Double totalPresentDays,
            BigDecimal totalWorkedHours,
            Long totalLateCount,
            Long totalLateMinutes,
            Long totalEarlyCount,
            BigDecimal totalOtHours,
            Double totalLeaveDays,
            Double totalAbsentDays
    ) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.totalPresentDays = totalPresentDays;
        this.totalWorkedHours = totalWorkedHours;
        this.totalLateCount = totalLateCount;
        this.totalLateMinutes = totalLateMinutes;
        this.totalEarlyCount = totalEarlyCount;
        this.totalOtHours = totalOtHours;
        this.totalLeaveDays = totalLeaveDays;
        this.totalAbsentDays = totalAbsentDays;
    }
}