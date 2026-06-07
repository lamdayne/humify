package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.mapper.EmployeeMapper;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.employee.validator.EmployeeValidator;
import com.lamdayne.humify.position.service.PositionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeValidator employeeValidator;
    private final EmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;
    private final CompanyAccessService companyAccessService;
    private final PositionAccessService positionAccessService;
    private final DepartmentAccessService departmentAccessService;

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        if (employeeRepository.existsByCompanyIdAndEmployeeCode(companyId, request.getEmployeeCode())) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_EXISTS);
        }

        employeeValidator.validateRefs(companyId, request);

        Employee employee = employeeMapper.toEmployee(request);
        employee.setCompany(companyAccessService.getReferenceById(companyId));
        employee.setBranch(branchAccessService.getReferenceById(request.getBranchId()));
        employee.setDepartment(departmentAccessService.getReferenceById(request.getDepartmentId()));
        employee.setPosition(positionAccessService.getReferenceById(request.getPositionId()));

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    public PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        List<EmployeeResponse> employees = employeePage.stream()
                .map(employeeMapper::toEmployeeResponse)
                .toList();

        return PageResponse.<EmployeeResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(employeePage.getTotalElements())
                .totalPages(employeePage.getTotalPages())
                .items(employees)
                .build();
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeMapper.updateEmployee(employee, request);

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    public void transferEmployee(Long id, TransferEmployeeRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeValidator.validateTransfer(companyId, request);

        employee.setBranch(branchAccessService.getReferenceById(request.getBranchId()));
        employee.setDepartment(departmentAccessService.getReferenceById(request.getDepartmentId()));

        employeeRepository.save(employee);
    }

    @Override
    public void updateEmployeeStatus(Long id, UpdateEmployeeStatusRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employee.setStatus(EmployeeStatus.valueOf(request.getStatus()));

        employeeRepository.save(employee);
    }

    @Override
    public void updateEmployeePosition(Long id, UpdateEmployeePositionRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeValidator.validatePosition(companyId, request);
        employee.setPosition(positionAccessService.getReferenceById(request.getPositionId()));

        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponse getByEmployeeCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode).orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return employeeMapper.toEmployeeResponse(employee);
    }
}
