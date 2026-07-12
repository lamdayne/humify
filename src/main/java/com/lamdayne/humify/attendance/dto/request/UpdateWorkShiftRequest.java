package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class UpdateWorkShiftRequest {
    private String name;
    private Instant startTime;
    private Instant endTime;
    private Instant breakStartTime;
    private Instant breakEndTime;

    @Min(value = 0, message = "GRACE_PERIOD_INVALID")
    private Integer gracePeriodMinutes;

    private Boolean status;
}
