package com.lamdayne.humify.attendance.controller;


import com.lamdayne.humify.attendance.dto.request.CreateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveTypeResponse;
import com.lamdayne.humify.attendance.service.LeaveTypeService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leave-types")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getLeaveTypes(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_TYPE_READ_SUCCESS,
                        leaveTypeService.getLeaveTypes(userPrincipal.getCompanyId())
                ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> createLeaveType(
            @Valid @RequestBody CreateLeaveTypeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_TYPE_CREATE_SUCCESS,
                        leaveTypeService.createLeaveType(userPrincipal.getCompanyId(), request)
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> updateLeaveType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeaveTypeRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_TYPE_UPDATE_SUCCESS,
                        leaveTypeService.updateLeaveType(id, userPrincipal.getCompanyId(), request)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        leaveTypeService.deleteLeaveType(id, userPrincipal.getCompanyId());
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.LEAVE_TYPE_DELETE_SUCCESS));
    }

}
