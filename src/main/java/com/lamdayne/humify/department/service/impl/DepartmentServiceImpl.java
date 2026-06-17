package com.lamdayne.humify.department.service.impl;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;
import com.lamdayne.humify.department.mapper.DepartmentMapper;
import com.lamdayne.humify.department.repository.DepartmentRepository;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.department.service.DepartmentService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService, DepartmentAccessService {

    private final DepartmentMapper departmentMapper;
    private final DepartmentRepository departmentRepository;
    private final BranchService branchService;


    @Override
    @Transactional
    public DepartmentResponse createDepartment(CreateDepartmentRequest request) {
        Branch branch = branchService.getBranchById(request.getBranchId());


        Department department = departmentMapper.toDepartment(request);

        department.setBranch(branch);

        return departmentMapper.toDepartmentResponse(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentResponse> getDepartmentByBranchId(Long branchId) {
        List<Department> departments = departmentRepository.findByBranchId(branchId);
        return departmentMapper.toDepartmentResponse(departments);
    }


    @Override
    public Department getReferenceById(Long id) {
        return departmentRepository.getReferenceById(id);
    }

    @Override
    public Department getById(Long id) {
        return departmentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long id) {
        return departmentRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndBranchId(Long id, Long branchId) {
        return departmentRepository.existsByIdAndBranchId(id, branchId);
    }
}
