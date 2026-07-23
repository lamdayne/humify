package com.lamdayne.humify.attendance.dto.response;


import com.lamdayne.humify.attendance.enums.AttendanceLogType;
import com.lamdayne.humify.attendance.enums.AttendanceVerifyMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class AttendanceLogResponse {

    private Long id;
    private Long employeeId;
    private Instant timestamp;
    private AttendanceLogType logType;
    private AttendanceVerifyMethod verifyMethod;
    private String ipAddress;
    private String deviceInfo;
}
