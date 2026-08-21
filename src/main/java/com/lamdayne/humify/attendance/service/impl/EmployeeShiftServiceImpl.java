package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.CreateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.response.EmployeeShiftResponse;
import com.lamdayne.humify.attendance.entity.EmployeeShift;
import com.lamdayne.humify.attendance.entity.WorkShift;
import com.lamdayne.humify.attendance.mapper.EmployeeShiftMapper;
import com.lamdayne.humify.attendance.repository.EmployeeShiftRepository;
import com.lamdayne.humify.attendance.repository.EmployeeShiftSpecification;
import com.lamdayne.humify.attendance.repository.WorkShiftRepository;
import com.lamdayne.humify.attendance.service.EmployeeShiftService;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.search.SearchCriteriaParser;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeShiftServiceImpl implements EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;
    private final EmployeeShiftSpecification employeeShiftSpecification;
    private final EmployeeShiftMapper employeeShiftMapper;

    @Override
    @Transactional
    public EmployeeShiftResponse assignShift(CreateEmployeeShiftRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        WorkShift workShift = workShiftRepository.findById(request.getWorkShiftId())
                .filter(ws -> ws.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        if (request.getEndDate() != null && request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.START_YEAR_AFTER_END_YEAR);
        }

        if (employeeShiftRepository.hasOverlap(request.getEmployeeId(), request.getStartDate(), request.getEndDate(), null)) {
            throw new AppException(ErrorCode.EMPLOYEE_SHIFT_OVERLAP);
        }

        EmployeeShift employeeShift = employeeShiftMapper.toEntity(request);
        employeeShift.setEmployee(employee);
        employeeShift.setWorkShift(workShift);

        return employeeShiftMapper.toResponse(employeeShiftRepository.save(employeeShift));
    }

    @Override
    public PageResponse<EmployeeShiftResponse> getEmployeeShifts(Pageable pageable, String[] searchParams) {
        Long companyId = CompanyContext.getCompanyId();
        List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(searchParams != null ? searchParams : new String[0]);
        Specification<EmployeeShift> spec = employeeShiftSpecification.build(criteriaList, companyId);

        Page<EmployeeShift> page = employeeShiftRepository.findAll(spec, pageable);
        List<EmployeeShiftResponse> responses = page.stream()
                .filter(es -> es.getDeletedAt() == null)
                .map(employeeShiftMapper::toResponse)
                .toList();

        return PageResponse.<EmployeeShiftResponse>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    public EmployeeShiftResponse getEmployeeShiftDetail(Long id) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .filter(es -> es.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SHIFT_NOT_FOUND));
        return employeeShiftMapper.toResponse(employeeShift);
    }

    @Override
    @Transactional
    public EmployeeShiftResponse updateEmployeeShift(Long id, UpdateEmployeeShiftRequest request) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .filter(es -> es.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SHIFT_NOT_FOUND));

        WorkShift workShift = employeeShift.getWorkShift();
        if (request.getWorkShiftId() != null) {
            workShift = workShiftRepository.findById(request.getWorkShiftId())
                    .filter(ws -> ws.getDeletedAt() == null)
                    .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        }

        var start = request.getStartDate() != null ? request.getStartDate() : employeeShift.getStartDate();
        var end = request.getEndDate() != null ? request.getEndDate() : employeeShift.getEndDate();

        if (end != null && start.isAfter(end)) {
            throw new AppException(ErrorCode.START_YEAR_AFTER_END_YEAR);
        }

        if (employeeShiftRepository.hasOverlap(employeeShift.getEmployee().getId(), start, end, id)) {
            throw new AppException(ErrorCode.EMPLOYEE_SHIFT_OVERLAP);
        }

        employeeShiftMapper.updateEntity(employeeShift, request);
        employeeShift.setWorkShift(workShift);
        if (request.getStartDate() != null) employeeShift.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) employeeShift.setEndDate(request.getEndDate());

        return employeeShiftMapper.toResponse(employeeShiftRepository.save(employeeShift));
    }

    @Override
    @Transactional
    public void deleteEmployeeShift(Long id) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .filter(es -> es.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_SHIFT_NOT_FOUND));

        employeeShift.setDeletedAt(Instant.now());
        employeeShiftRepository.save(employeeShift);
    }
}
