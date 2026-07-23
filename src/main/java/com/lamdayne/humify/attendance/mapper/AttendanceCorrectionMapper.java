package com.lamdayne.humify.attendance.mapper;


import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.entity.AttendanceCorrection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttendanceCorrectionMapper {

    @Mapping(source = "attendance.id", target = "attendanceId")
    @Mapping(source = "correctionDate", target = "workDate")
    @Mapping(source = "approver.employee.fullName", target = "approverName")
    AttendanceCorrectionResponse toResponse(AttendanceCorrection correction);

}
