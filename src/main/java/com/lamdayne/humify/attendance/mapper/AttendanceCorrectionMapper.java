package com.lamdayne.humify.attendance.mapper;

import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.entity.AttendanceCorrection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AttendanceCorrectionMapper {

    @Mapping(target = "attendanceId", source = "attendance.id")
    @Mapping(target = "workDate", source = "correctionDate")
    @Mapping(target = "approverName", source = "correction", qualifiedByName = "mapApproverName")
    AttendanceCorrectionResponse toResponse(AttendanceCorrection correction);

    @Named("mapApproverName")
    default String mapApproverName(AttendanceCorrection correction) {
        if (correction == null) return null;

        if (correction.getApprover() != null && correction.getApprover().getEmployee() != null) {
            return correction.getApprover().getEmployee().getFullName();
        }
        return null;
    }
}