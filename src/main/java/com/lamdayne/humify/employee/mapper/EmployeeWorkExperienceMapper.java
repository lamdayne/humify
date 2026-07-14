package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.request.CreateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeWorkExperienceResponse;
import com.lamdayne.humify.employee.entity.EmployeeWorkExperience;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeeWorkExperienceMapper {

    EmployeeWorkExperience toEntity(CreateEmployeeWorkExperienceRequest request);

    EmployeeWorkExperienceResponse toResponse(EmployeeWorkExperience entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(
            @MappingTarget EmployeeWorkExperience entity,
            UpdateEmployeeWorkExperienceRequest request
    );

}