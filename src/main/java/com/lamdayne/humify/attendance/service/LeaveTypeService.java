package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.CreateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveTypeResponse;
import com.lamdayne.humify.attendance.entity.LeaveType;

import java.util.List;

public interface LeaveTypeService {

    List<LeaveTypeResponse> getLeaveTypes(Long companyId);
    LeaveTypeResponse createLeaveType(Long companyId, CreateLeaveTypeRequest request);
    LeaveTypeResponse updateLeaveType(Long id, Long companyId, UpdateLeaveTypeRequest request);
    void deleteLeaveType(Long id, Long companyId);
}
