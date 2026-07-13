package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.AttendanceApprovalRequest;
import com.lamdayne.humify.attendance.dto.request.AttendanceCorrectionRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import com.lamdayne.humify.attendance.service.AttendanceCorrectionService;
import com.lamdayne.humify.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance-corrections")
@RequiredArgsConstructor
public class AttendanceCorrectionController {

    private final AttendanceCorrectionService correctionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCorrection(
            @Valid @RequestBody AttendanceCorrectionRequest request,
            @AuthenticationPrincipal User currentUser) {

        AttendanceCorrectionResponse data = correctionService.createCorrection(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildApiResponse(
                "ATTENDANCE_CORRECTION_CREATE_SUCCESS",
                "Attendance correction request created successfully",
                data));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_APPROVE', 'FULL_ACCESS')")
    public ResponseEntity<Page<AttendanceCorrectionResponse>> getCorrectionsForManager(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AttendanceCorrectionStatus status,
            @RequestParam(required = false) Long employeeId) {

        Page<AttendanceCorrectionResponse> data = correctionService.getCorrectionsForManager(status, employeeId, page, size);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/me")
    public ResponseEntity<List<AttendanceCorrectionResponse>> getMyCorrections(
            @RequestParam(required = false) AttendanceCorrectionStatus status,
            @AuthenticationPrincipal User currentUser) {

        List<AttendanceCorrectionResponse> data = correctionService.getMyCorrections(status, currentUser);
        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_APPROVE', 'FULL_ACCESS')")
    public ResponseEntity<Map<String, Object>> approveCorrection(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceApprovalRequest request,
            @AuthenticationPrincipal User currentUser) {

        AttendanceCorrectionResponse data = correctionService.approveCorrection(id, request, currentUser);
        return ResponseEntity.ok(buildApiResponse(
                "ATTENDANCE_CORRECTION_APPROVE_SUCCESS",
                "Attendance correction request approved successfully",
                data));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ATTENDANCE_APPROVE', 'FULL_ACCESS')")
    public ResponseEntity<Map<String, Object>> rejectCorrection(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceApprovalRequest request,
            @AuthenticationPrincipal User currentUser) {

        AttendanceCorrectionResponse data = correctionService.rejectCorrection(id, request, currentUser);
        return ResponseEntity.ok(buildApiResponse(
                "ATTENDANCE_CORRECTION_REJECT_SUCCESS",
                "Attendance correction request rejected successfully",
                data));
    }

    private Map<String, Object> buildApiResponse(String code, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}