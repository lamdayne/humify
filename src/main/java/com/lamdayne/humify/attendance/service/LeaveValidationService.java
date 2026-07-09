package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;

import java.time.LocalDate;

public interface LeaveValidationService {

    void validateDate(LocalDate startDate, LocalDate endDate, LeaveSessionType sessionType);

    void validateAttachment(LeaveType leaveType, String attachmentUrl);

    void validateOverlap(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludingRequestId);

}
