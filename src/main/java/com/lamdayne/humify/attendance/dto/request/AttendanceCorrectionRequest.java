package com.lamdayne.humify.attendance.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter

public class AttendanceCorrectionRequest {

    @NotNull(message = "ATTENDANCE_ID_REQUIRED")
    private Long attendanceId;

    private Instant requestedCheckIn;

    private Instant requestedCheckOut;

    @NotBlank(message = "CORRECTION_REASON_REQUIRED")
    private String reason;
}
