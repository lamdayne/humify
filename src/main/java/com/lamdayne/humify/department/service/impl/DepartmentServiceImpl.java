package com.lamdayne.humify.department.service.impl;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.request.UpdateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.entity.Department;
import com.lamdayne.humify.department.mapper.DepartmentMapper;
import com.lamdayne.humify.department.repository.DepartmentRepository;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.department.service.DepartmentService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Transactional
    public PageResponse<DepartmentResponse> getDepartmentByBranchId(Long branchId, int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Department> departments = departmentRepository.findByBranchId(branchId, pageable);
        List<DepartmentResponse> departmentResponses = departments.stream().map(departmentMapper::toDepartmentResponse).toList();

        return PageResponse.<DepartmentResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(departments.getTotalPages())
                .totalElements(departments.getTotalElements())
                .items(departmentResponses)
                .build();
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_NOT_FOUND));

        departmentMapper.updateDepartment(department, request);

        return departmentMapper.toDepartmentResponse(departmentRepository.save(department));
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
