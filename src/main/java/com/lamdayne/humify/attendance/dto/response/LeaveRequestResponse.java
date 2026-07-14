package com.lamdayne.humify.attendance.dto.response;

import com.lamdayne.humify.attendance.enums.LeaveRequestStatus;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class LeaveRequestResponse {
    private Long id;
    private Long employeeId;
    private ShortLeaveTypeResponse leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal durationDays;
    private LeaveSessionType sessionType;
    private String reason;
    private String attachmentUrl;
    private LeaveRequestStatus status;
    private String approverId;
    private String approverNote;
    private Instant approvedAt;
    private Instant createdAt;
}
