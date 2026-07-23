package com.lamdayne.humify.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
public class WorkShiftResponse {
    private Long id;
    private String shiftCode;
    private String name;
    private Instant startTime;
    private Instant endTime;
    private Instant breakStartTime;
    private Instant breakEndTime;
    private Integer gracePeriodMinutes;
    private Boolean status;
}