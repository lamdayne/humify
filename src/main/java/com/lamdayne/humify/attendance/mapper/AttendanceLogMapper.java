package com.lamdayne.humify.attendance.mapper;


import com.lamdayne.humify.attendance.dto.response.AttendanceLogResponse;
import com.lamdayne.humify.attendance.entity.AttendanceLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceLogMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    AttendanceLogResponse toResponse(AttendanceLog log);
}
