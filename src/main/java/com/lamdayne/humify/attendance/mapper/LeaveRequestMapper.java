package com.lamdayne.humify.attendance.mapper;

import com.lamdayne.humify.attendance.dto.request.CreateLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveRequestResponse;
import com.lamdayne.humify.attendance.entity.LeaveRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {

    LeaveRequest toLeaveRequest(CreateLeaveRequest request);

    @Mapping(target = "employeeId", source = "employee.id")
    LeaveRequestResponse toLeaveRequestResponse(LeaveRequest leaveRequest);

    void updateLeaveRequest(@MappingTarget LeaveRequest leaveRequest, UpdateLeaveRequest request);

}
