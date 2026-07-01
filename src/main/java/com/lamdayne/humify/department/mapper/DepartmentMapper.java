package com.lamdayne.humify.department.mapper;



import com.lamdayne.humify.company.dto.request.UpdateCompanyRequest;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.request.UpdateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department toDepartment(CreateDepartmentRequest request);
    @Mapping(source = "branch.id", target = "branchId")
    DepartmentResponse toDepartmentResponse(Department department);
    List<DepartmentResponse> toDepartmentResponse(List<Department> departments);
    @Mapping(target = "branch", ignore = true)
    void updateDepartment(@MappingTarget Department department, UpdateDepartmentRequest request);
}
