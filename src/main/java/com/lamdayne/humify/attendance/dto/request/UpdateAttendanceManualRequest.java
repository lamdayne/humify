package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class UpdateAttendanceManualRequest {

    private Instant manualCheckIn;
    private Instant manualCheckOut;
    private Long workShiftId;
    private BigDecimal otHours;

    @NotNull(message = "VALIDATION_ERROR")
    @DecimalMin(value = "0.0", message = "WORK_POINTS_INVALID")
    @DecimalMax(value = "2.0", message = "WORK_POINTS_INVALID")
    private BigDecimal workPoints;

    @NotBlank(message = "VALIDATION_ERROR")
    private String status;

    @NotBlank(message = "MODIFICATION_REASON_REQUIRED")
    private String modificationReason;
}