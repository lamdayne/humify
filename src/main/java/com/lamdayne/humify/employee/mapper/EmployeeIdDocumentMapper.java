package com.lamdayne.humify.employee.mapper;

import com.lamdayne.humify.employee.dto.request.EmployeeIdDocumentRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeIdDocumentResponse;
import com.lamdayne.humify.employee.entity.EmployeeIdDocument;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeeIdDocumentMapper {

    @Mapping(source = "employee.id", target = "employeeId")
    EmployeeIdDocumentResponse toResponse(EmployeeIdDocument document);

    EmployeeIdDocument toEntity(EmployeeIdDocumentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget EmployeeIdDocument document, EmployeeIdDocumentRequest request);
}