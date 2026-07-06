package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.request.CreateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeCertificationResponse;
import com.lamdayne.humify.employee.entity.EmployeeCertification;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeeCertificationMapper {

    EmployeeCertification toEmployeeCertification(CreateEmployeeCertificationRequest request);

    EmployeeCertificationResponse toEmployeeCertificationResponse(EmployeeCertification entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEmployeeCertification(
            @MappingTarget EmployeeCertification employeeCertification,
            UpdateEmployeeCertificationRequest request
    );

}
