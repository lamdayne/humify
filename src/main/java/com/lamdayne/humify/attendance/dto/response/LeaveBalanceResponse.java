package com.lamdayne.humify.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LeaveBalanceResponse {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private LeaveTypeShortResponse leaveType;
    private Integer year;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
    private BigDecimal pendingDays;
    private BigDecimal remainingDays;

    @Getter
    @Builder
    public static class LeaveTypeShortResponse {
        private Long id;
        private String name;
        private String code;
    }
}