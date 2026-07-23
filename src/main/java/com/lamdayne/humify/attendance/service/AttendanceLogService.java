package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.WebSwipeRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceLogResponse;
import com.lamdayne.humify.common.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceLogService {

    AttendanceLogResponse registerWebSwipe(String email, Long companyId, WebSwipeRequest request, String ipAddress, String userAgent);
    PageResponse<AttendanceLogResponse> getAllLogsForHr(String search, int page, int size);
    List<AttendanceLogResponse> getMyLogs(String email, Long companyId, LocalDate startDate, LocalDate endDate);
}
