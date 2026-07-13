package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class AttendanceCorrectionRequest {
    @NotNull(message = "Attendance ID không được để trống")
    private Long attendanceId;

    private Instant requestedCheckIn;
    private Instant requestedCheckOut;

    @NotBlank(message = "Lý do giải trình không được để trống")
    private String reason;
}