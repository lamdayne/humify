package com.lamdayne.humify.attendance.dto.request;


import com.lamdayne.humify.attendance.enums.AttendanceLogType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSwipeRequest {

    @NotNull(message = "LOG_TYPE_REQUIRED")
    private AttendanceLogType logType;
}
