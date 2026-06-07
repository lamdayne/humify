package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.request.CreateEmployeeRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.entity.Employee;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    Employee toEmployee(CreateEmployeeRequest request);

    EmployeeResponse toEmployeeResponse(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployee(@MappingTarget Employee employee, UpdateEmployeeRequest request);

}
