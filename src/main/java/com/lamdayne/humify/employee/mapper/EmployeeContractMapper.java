package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.request.CreateContractRequest;
import com.lamdayne.humify.employee.dto.request.UpdateContractRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeContractResponse;
import com.lamdayne.humify.employee.entity.EmployeeContract;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EmployeeContractMapper {

    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "employee.id", target = "employeeId")
    EmployeeContractResponse toResponse(EmployeeContract entity);

    EmployeeContract toEntity(CreateContractRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget EmployeeContract entity, UpdateContractRequest request);
}