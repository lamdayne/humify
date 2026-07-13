package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.AttendanceApprovalRequest;
import com.lamdayne.humify.attendance.dto.request.AttendanceCorrectionRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import com.lamdayne.humify.user.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AttendanceCorrectionService {

    AttendanceCorrectionResponse createCorrection(AttendanceCorrectionRequest request, User currentUser);

    Page<AttendanceCorrectionResponse> getCorrectionsForManager(AttendanceCorrectionStatus status, Long employeeId, int page, int size);

    List<AttendanceCorrectionResponse> getMyCorrections(AttendanceCorrectionStatus status, User currentUser);

    AttendanceCorrectionResponse approveCorrection(Long id, AttendanceApprovalRequest request, User currentUser);

    AttendanceCorrectionResponse rejectCorrection(Long id, AttendanceApprovalRequest request, User currentUser);
}