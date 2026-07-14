package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.AttendanceCorrectionRequest;
import com.lamdayne.humify.attendance.dto.request.CorrectionActionRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.service.AttendanceCorrectionService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance-corrections")
public class AttendanceCorrectionController {

    private final AttendanceCorrectionService correctionService;

    // 4.1. Tạo đơn giải trình
    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceCorrectionResponse>> createCorrection(
            @Valid @RequestBody AttendanceCorrectionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_CORRECTION_CREATE_SUCCESS,
                        // Bỏ companyId đi, chỉ truyền username (email)
                        correctionService.createCorrection(userPrincipal.getUsername(), request)
                ));
    }

    // 4.2. HR/Manager lấy danh sách toàn bộ đơn
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceCorrectionResponse>>> getAllCorrections(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_CORRECTION_READ_SUCCESS,
                        correctionService.getAllCorrectionsForHr(status, employeeId, page, size)
                ));
    }

    // 4.3. Cá nhân lấy danh sách đơn của mình
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceCorrectionResponse>>> getMyCorrections(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_CORRECTION_READ_SUCCESS,
                        // Bỏ companyId đi, chỉ truyền username (email)
                        correctionService.getMyCorrections(userPrincipal.getUsername(), status, page, size)
                ));
    }

    // 4.4. Duyệt đơn
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AttendanceCorrectionResponse>> approveCorrection(
            @PathVariable Long id,
            @Valid @RequestBody CorrectionActionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_CORRECTION_APPROVE_SUCCESS,
                        correctionService.approveCorrection(id, userPrincipal.getId(), request)
                ));
    }

    // 4.5. Từ chối đơn
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AttendanceCorrectionResponse>> rejectCorrection(
            @PathVariable Long id,
            @Valid @RequestBody CorrectionActionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ATTENDANCE_CORRECTION_REJECT_SUCCESS,
                        correctionService.rejectCorrection(id, userPrincipal.getId(), request)
                ));
    }
}