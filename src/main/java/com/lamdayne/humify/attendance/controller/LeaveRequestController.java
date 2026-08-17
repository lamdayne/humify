package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.ApproveLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.CreateLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.RejectLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveRequestResponse;
import com.lamdayne.humify.attendance.service.LeaveRequestService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_CREATE', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> createLeaveRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid CreateLeaveRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_CREATE_SUCCESS,
                        leaveRequestService.createLeaveRequest(userPrincipal, request)
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_READ', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> getLeaveRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long id
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_READ_SUCCESS,
                        "Get leave request successfully",
                        leaveRequestService.getLeaveRequestById(userPrincipal, id)
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_UPDATE', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> updateLeaveRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long id,
            @RequestBody @Valid UpdateLeaveRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_UPDATE_SUCCESS,
                        leaveRequestService.updateLeaveRequest(userPrincipal, id, request)
                ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_CANCEL', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<Void>> cancelLeaveRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long id
    ) {
        leaveRequestService.cancelLeaveRequest(userPrincipal, id);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LEAVE_REQUEST_CANCELLED_SUCCESS));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_APPROVE', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeaveRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long id,
            @RequestBody @Valid ApproveLeaveRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_APPROVE_SUCCESS,
                        leaveRequestService.approveLeaveRequest(userPrincipal, id, request)
                ));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('LEAVE_REQUEST_REJECT', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeaveRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long id,
            @RequestBody @Valid RejectLeaveRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_REJECT_SUCCESS,
                        leaveRequestService.rejectLeaveRequest(userPrincipal, id, request)
                ));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestResponse>>> getMyLeaveRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable,
            @RequestParam(required = false) String[] leaveRequest
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_READ_SUCCESS,
                        leaveRequestService.getMyLeaveRequests(userPrincipal, pageable, leaveRequest)
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'LEAVE_REQUEST_READ_ALL', 'LEAVE_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestResponse>>> getLeaveRequests(
            Pageable pageable,
            @RequestParam(required = false) String[] leaveRequest
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.LEAVE_REQUEST_READ_SUCCESS,
                        leaveRequestService.getLeaveRequests(pageable, leaveRequest)
                ));
    }

}
