package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.AttendanceCorrectionRequest;
import com.lamdayne.humify.attendance.dto.request.CorrectionActionRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.common.response.PageResponse;

public interface AttendanceCorrectionService {

    AttendanceCorrectionResponse createCorrection(String email, AttendanceCorrectionRequest request);
    PageResponse<AttendanceCorrectionResponse> getMyCorrections(String email, String status, int page, int size);
    PageResponse<AttendanceCorrectionResponse> getAllCorrectionsForHr(String status, Long employeeId, int page, int size);
    AttendanceCorrectionResponse approveCorrection(Long correctionId, Long approverUserId, CorrectionActionRequest request);
    AttendanceCorrectionResponse rejectCorrection(Long correctionId, Long approverUserId, CorrectionActionRequest request);
}
