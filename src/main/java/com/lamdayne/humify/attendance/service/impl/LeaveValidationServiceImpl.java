package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;
import com.lamdayne.humify.attendance.repository.LeaveRequestRepository;
import com.lamdayne.humify.attendance.service.LeaveValidationService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class LeaveValidationServiceImpl implements LeaveValidationService {

    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public void validateDate(LocalDate startDate, LocalDate endDate, LeaveSessionType sessionType) {
        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_DATE_INVALID);
        }

        if (sessionType != LeaveSessionType.FULL_DAY && !startDate.isEqual(endDate)) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_DATE_INVALID);
        }
    }

    @Override
    public void validateAttachment(LeaveType leaveType, String attachmentUrl) {
        boolean isBlank = attachmentUrl == null || attachmentUrl.isBlank();
        if (Boolean.TRUE.equals(leaveType.getRequiresAttachment()) && isBlank) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_ATTACHMENT_REQUIRED);
        }
    }

    @Override
    public void validateOverlap(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludingRequestId) {
        boolean hasOverlap = (excludingRequestId == null)
                ? leaveRequestRepository.existsOverlapRequest(employeeId, startDate, endDate)
                : leaveRequestRepository.existsOverlapRequestExcludingId(employeeId, startDate, endDate, excludingRequestId);

        if (hasOverlap) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_DATE_OVERLAP);
        }
    }
}
