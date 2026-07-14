package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.WebSwipeRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceLogResponse;
import com.lamdayne.humify.attendance.service.AttendanceLogService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance-logs")

public class AttendanceLogController {

    private final AttendanceLogService attendanceLogService;

    @PostMapping("/web-swipe")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> webSwipe(
            @Valid @RequestBody WebSwipeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest servletRequest) {

        String ipAddress = IpUtils.getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_LOG_CREATE_SUCCESS,
                        attendanceLogService.registerWebSwipe(
                                userPrincipal.getUsername(),
                                userPrincipal.getCompanyId(),
                                request,
                                ipAddress,
                                userAgent
                        )
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceLogResponse>>> getAllLogs(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_LOG_READ_SUCCESS,
                        attendanceLogService.getAllLogsForHr(search, page, size)
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AttendanceLogResponse>>> getMyLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_LOG_READ_SUCCESS,
                        attendanceLogService.getMyLogs(
                                userPrincipal.getUsername(),
                                userPrincipal.getCompanyId(),
                                startDate,
                                endDate)
                ));
    }
}
