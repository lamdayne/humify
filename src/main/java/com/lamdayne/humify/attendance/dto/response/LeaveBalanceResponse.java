package com.lamdayne.humify.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeaveBalanceResponse {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private LeaveTypeShortResponse leaveType;
    private Integer year;
    private Integer allocatedDays;
    private Integer usedDays;
    private Integer pendingDays;
    private Integer remainingDays;

    @Getter
    @Builder
    public static class LeaveTypeShortResponse {
        private Long id;
        private String name;
        private String code;
    }
}