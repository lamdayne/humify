package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeCertificationResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeCertification;
import com.lamdayne.humify.employee.mapper.EmployeeCertificationMapper;
import com.lamdayne.humify.employee.repository.EmployeeCertificationRepository;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeCertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeCertificationServiceImpl implements EmployeeCertificationService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeCertificationMapper employeeCertificationMapper;
    private final EmployeeCertificationRepository employeeCertificationRepository;


    @Override
    public EmployeeCertificationResponse addEmployeeCertification(Long employeeId, CreateEmployeeCertificationRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeCertification employeeCert = employeeCertificationMapper.toEmployeeCertification(request);
        employeeCert.setEmployee(employee);

        return employeeCertificationMapper.toEmployeeCertificationResponse(
                employeeCertificationRepository.save(employeeCert)
        );
    }

    @Override
    public EmployeeCertificationResponse updateEmployeeCertification(
            UserPrincipal userPrincipal,
            Long employeeId,
            Long id,
            UpdateEmployeeCertificationRequest request
    ) {
        EmployeeCertification employeeCert = employeeCertificationRepository.findByIdAndEmployeeId(id, employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_CERTIFICATION_NOT_FOUND));

        Employee employee = employeeCert.getEmployee();

        validateSameCompany(userPrincipal, employee);

        employeeCertificationMapper.updateEmployeeCertification(employeeCert , request);

        return employeeCertificationMapper.toEmployeeCertificationResponse(
                employeeCertificationRepository.save(employeeCert)
        );
    }

    @Override
    public EmployeeCertificationResponse getEmployeeCertification(UserPrincipal userPrincipal, Long employeeId, Long id) {
        EmployeeCertification employeeCert = employeeCertificationRepository
                .findByIdAndEmployeeId(id, employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_CERTIFICATION_NOT_FOUND));

        Employee employee = employeeCert.getEmployee();

        validateSameCompany(userPrincipal, employee);

        return employeeCertificationMapper.toEmployeeCertificationResponse(employeeCert);
    }

    @Override
    public void deleteEmployeeCertification(Long employeeId, Long id) {
        EmployeeCertification employeeCert = employeeCertificationRepository
                .findByIdAndEmployeeId(id, employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_CERTIFICATION_NOT_FOUND));

        employeeCertificationRepository.delete(employeeCert);
    }

    @Override
    public PageResponse<EmployeeCertificationResponse> getEmployeeCertifications(Long employeeId, int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<EmployeeCertification> employeeCertificationPage = employeeCertificationRepository
                .findByEmployeeId(employeeId, pageable);

        List<EmployeeCertificationResponse> employeeCertificationResponses = employeeCertificationPage.stream()
                .map(employeeCertificationMapper::toEmployeeCertificationResponse)
                .toList();

        return PageResponse.<EmployeeCertificationResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(employeeCertificationPage.getTotalElements())
                .totalPages(employeeCertificationPage.getTotalPages())
                .items(employeeCertificationResponses)
                .build();
    }

    private void validateSameCompany(UserPrincipal userPrincipal, Employee employee) {
        boolean sameCompany = Objects.equals(
                userPrincipal.getCompanyId(),
                employee.getCompany() != null ? employee.getCompany().getId() : null
        );

        if (!sameCompany) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

}
