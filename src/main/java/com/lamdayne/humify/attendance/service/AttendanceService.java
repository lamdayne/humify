package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.UpdateAttendanceManualRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceDetailResponse;
import com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    PageResponse<AttendanceDetailResponse> getHRView(LocalDate start, LocalDate end, Long employeeId, AttendanceStatus status, Pageable pageable);
    List<AttendanceDetailResponse> getPersonalView(Long userId, LocalDate start, LocalDate end);
    List<AttendanceSummaryReportResponse> getSummaryReport(LocalDate start, LocalDate end, Long employeeId);
    AttendanceDetailResponse updateManual(Long id, Long currentUserId, UpdateAttendanceManualRequest request);
}