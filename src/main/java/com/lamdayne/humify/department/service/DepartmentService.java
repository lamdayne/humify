package com.lamdayne.humify.department.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.request.UpdateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(CreateDepartmentRequest request);
    PageResponse<DepartmentResponse> getDepartmentByBranchId(Long branchId, int page, int size, String... sorts);
    DepartmentResponse updateDepartment(long id, UpdateDepartmentRequest request);
}
