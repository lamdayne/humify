package com.lamdayne.humify.attendance.dto.request;

import com.lamdayne.humify.attendance.enums.LeaveSessionType;
import com.lamdayne.humify.common.validator.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequest {

    @NotNull(message = "LEAVE_TYPE_ID_REQUIRED")
    private Long leaveTypeId;

    @NotNull(message = "LEAVE_REQUEST_START_DATE_REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "LEAVE_REQUEST_END_DATE_REQUIRED")
    private LocalDate endDate;

    @EnumValue(name = "sessionType", message = "LEAVE_SESSION_TYPE_INVALID", enumClass = LeaveSessionType.class)
    private String sessionType;

    @NotBlank(message = "LEAVE_REQUEST_REASON_REQUIRED")
    private String reason;

    private String attachmentUrl;

}
