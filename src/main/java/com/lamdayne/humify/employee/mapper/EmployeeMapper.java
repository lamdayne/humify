package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.request.CreateEmployeeRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    Employee toEmployee(CreateEmployeeRequest request);
  
    @Mapping(source = "employee.id", target = "id")
    @Mapping(source = "employee.branch.name", target = "branchName")
    @Mapping(source = "employee.department.name", target = "departmentName")
    @Mapping(source = "employee.position.name", target = "positionName")
    EmployeeResponse toEmployeeResponse(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployee(@MappingTarget Employee employee, UpdateEmployeeRequest request);

}
