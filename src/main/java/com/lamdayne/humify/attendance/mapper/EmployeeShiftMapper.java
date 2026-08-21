package com.lamdayne.humify.attendance.mapper;

import com.lamdayne.humify.attendance.dto.request.CreateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.response.EmployeeShiftResponse;
import com.lamdayne.humify.attendance.entity.EmployeeShift;
import com.lamdayne.humify.employee.mapper.EmployeeMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {EmployeeMapper.class, WorkShiftMapper.class})
public interface EmployeeShiftMapper {

    @Mapping(source = "employee", target = "employee")
    @Mapping(source = "workShift", target = "workShift")
    EmployeeShiftResponse toResponse(EmployeeShift entity);

    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "workShift", ignore = true)
    EmployeeShift toEntity(CreateEmployeeShiftRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "workShift", ignore = true)
    void updateEntity(@MappingTarget EmployeeShift entity, UpdateEmployeeShiftRequest request);
}
