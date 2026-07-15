package com.lamdayne.humify.department.service;

import com.lamdayne.humify.common.base.BaseAccessService;
import com.lamdayne.humify.department.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentAccessService extends BaseAccessService<Department, Long> {

    boolean existsByIdAndBranchId(Long id, Long branchId);

    Optional<Department> findByNameAndBranchId(String name, Long branchId);

    List<Department> findByBranchId(Long branchId);

}
