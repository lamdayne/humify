package com.lamdayne.humify.attendance.mapper;


import com.lamdayne.humify.attendance.dto.response.LeaveTypeResponse;
import com.lamdayne.humify.attendance.entity.LeaveType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeaveTypeMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "paid", target = "isPaid")
    LeaveTypeResponse toResponse(LeaveType leaveType);
}
