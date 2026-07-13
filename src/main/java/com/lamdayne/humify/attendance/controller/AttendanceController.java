package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.UpdateAttendanceManualRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceDetailResponse;
import com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.service.AttendanceService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.common.util.PageableUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceDetailResponse>>> getHRView(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(required = false) String... sorts
    ) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        PageResponse<AttendanceDetailResponse> response = attendanceService.getHRView(startDate, endDate, employeeId, status, pageable);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ATTENDANCE_READ_SUCCESS, response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AttendanceDetailResponse>>> getPersonalView(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<AttendanceDetailResponse> response = attendanceService.getPersonalView(userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ATTENDANCE_READ_SUCCESS, response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<AttendanceSummaryReportResponse>>> getSummaryReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long employeeId
    ) {
        List<AttendanceSummaryReportResponse> response = attendanceService.getSummaryReport(startDate, endDate, employeeId);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ATTENDANCE_READ_SUCCESS, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceDetailResponse>> updateManual(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateAttendanceManualRequest request
    ) {
        AttendanceDetailResponse response = attendanceService.updateManual(id, userPrincipal.getId(), request);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ATTENDANCE_UPDATE_SUCCESS, response));
    }
}