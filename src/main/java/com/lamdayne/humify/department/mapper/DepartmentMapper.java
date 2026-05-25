package com.lamdayne.humify.department.mapper;



import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    Department toDepartment(CreateDepartmentRequest request);
    @Mapping(source = "branch.id", target = "branchId")
    List<DepartmentResponse> toDepartmentResponse(List<Department> departments);
    DepartmentResponse toDepartmentResponse(Department department);
}
