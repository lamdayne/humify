package com.lamdayne.humify.attendance.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class AttendanceDetailResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long workShiftId;
    private String workShiftCode;
    private LocalDate workDate;
    private Instant checkInTime;
    private Instant checkOutTime;
    private BigDecimal workedHours;
    private Integer lateMinutes;
    private Integer earlyMinutes;
    private BigDecimal otHours;
    private BigDecimal workPoints;
    private String checkedStatus;
    private String status;
    private Boolean isModified;
}