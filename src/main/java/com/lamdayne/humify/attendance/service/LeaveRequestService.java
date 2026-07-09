package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.ApproveLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.CreateLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.RejectLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveRequestResponse;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;

public interface LeaveRequestService {

    LeaveRequestResponse createLeaveRequest(UserPrincipal userPrincipal, CreateLeaveRequest request);

    LeaveRequestResponse getLeaveRequestById(UserPrincipal userPrincipal, Long id);

    LeaveRequestResponse updateLeaveRequest(UserPrincipal userPrincipal, Long id, UpdateLeaveRequest request);

    void cancelLeaveRequest(UserPrincipal userPrincipal, Long id);

    LeaveRequestResponse approveLeaveRequest(UserPrincipal userPrincipal, Long id, ApproveLeaveRequest request);

    LeaveRequestResponse rejectLeaveRequest(UserPrincipal userPrincipal, Long id, RejectLeaveRequest request);

}
