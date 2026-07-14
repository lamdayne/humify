package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.UpdateAttendanceManualRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceDetailResponse;
import com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse;
import com.lamdayne.humify.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    PageResponse<AttendanceDetailResponse> getHRView(Pageable pageable, String[] searchParams);
    List<AttendanceDetailResponse> getPersonalView(Long userId, String[] searchParams);
    List<AttendanceSummaryReportResponse> getSummaryReport(LocalDate start, LocalDate end);
    AttendanceDetailResponse updateManual(Long id, Long currentUserId, UpdateAttendanceManualRequest request);
}