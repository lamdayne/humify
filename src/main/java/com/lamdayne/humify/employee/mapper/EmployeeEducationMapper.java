package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.response.EmployeeEducationResponse;
import com.lamdayne.humify.employee.entity.EmployeeEducation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeEducationMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    EmployeeEducationResponse toResponse(EmployeeEducation education);
}
