package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.UpdateLeaveBalanceRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveBalanceResponse;
import com.lamdayne.humify.attendance.entity.LeaveBalance;
import com.lamdayne.humify.attendance.entity.LeaveRequest;
import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LeaveBalanceService {

    List<LeaveBalanceResponse> getLeaveBalances(Long employeeId, Integer year);

    LeaveBalanceResponse updateLeaveBalance(Long employeeId, UpdateLeaveBalanceRequest request);

    void verifyAndReverseBalance(
            Long employeeId,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            LeaveSessionType sessionType,
            BigDecimal totalDurations
    );

    void updateLeaveBalanceForUpdate(
            Long employeeId,
            LeaveType leaveType,
            LeaveRequest oldRequest,
            UpdateLeaveRequest newRequest,
            BigDecimal newDuration
    );

    void save(LeaveBalance leaveBalance);

    void releasePendingBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest);

    void releaseUsedBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest);

    void approveLeaveBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest);

    void rejectLeaveBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest);

}