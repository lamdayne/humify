package com.lamdayne.humify.attendance.dto.response;

import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import lombok.*; // Import đầy đủ lombok
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCorrectionResponse {
    private Long id;
    private Long attendanceId;
    private LocalDate workDate;
    private Instant requestedCheckIn;
    private Instant requestedCheckOut;
    private String reason;
    private AttendanceCorrectionStatus status;
    private String approverName;
    private Instant approvedAt;
}