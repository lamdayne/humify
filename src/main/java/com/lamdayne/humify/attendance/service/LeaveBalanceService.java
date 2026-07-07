package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.UpdateLeaveBalanceRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveBalanceResponse;

import java.util.List;

public interface LeaveBalanceService {
    List<LeaveBalanceResponse> getLeaveBalances(Long employeeId, Integer year);
    LeaveBalanceResponse updateLeaveBalance(Long employeeId, UpdateLeaveBalanceRequest request);
}