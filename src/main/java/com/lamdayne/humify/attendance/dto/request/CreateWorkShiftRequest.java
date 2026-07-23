package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class CreateWorkShiftRequest {

    @NotBlank(message = "SHIFT_CODE_REQUIRED")
    @Size(max = 50, message = "INVALID_FIELD_NAME")
    private String shiftCode;

    @NotBlank(message = "SHIFT_NAME_REQUIRED")
    private String name;

    @NotNull(message = "SHIFT_START_TIME_REQUIRED")
    private Instant startTime;

    @NotNull(message = "SHIFT_END_TIME_REQUIRED")
    private Instant endTime;

    private Instant breakStartTime;
    private Instant breakEndTime;

    @Min(value = 0, message = "GRACE_PERIOD_INVALID")
    private Integer gracePeriodMinutes = 5;
}