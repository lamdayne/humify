package com.lamdayne.humify.department.service.impl;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.repository.BranchRepository;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;
import com.lamdayne.humify.department.mapper.DepartmentMapper;
import com.lamdayne.humify.department.repository.DepartmentRepository;
import com.lamdayne.humify.department.service.DepartmentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

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


}
