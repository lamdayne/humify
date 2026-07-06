package com.lamdayne.humify.employee.service.impl;


import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeEducationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeEducationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeEducationResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeEducation;
import com.lamdayne.humify.employee.mapper.EmployeeEducationMapper;
import com.lamdayne.humify.employee.repository.EmployeeEducationRepository;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeEducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeEducationServiceImpl implements EmployeeEducationService {

    private final EmployeeEducationRepository educationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeEducationMapper educationMapper;

    @Override
    @Transactional
    public EmployeeEducationResponse createEducation(Long employeeId, CreateEmployeeEducationRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeEducation education = EmployeeEducation.builder()
                .employee(employee)
                .degreeLevel(request.getDegreeLevel())
                .schoolName(request.getSchoolName())
                .major(request.getMajor())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .gpa(request.getGpa())
                .certificateFileUrl(request.getCertificateFileUrl())
                .note(request.getNote())
                .build();

        return educationMapper.toResponse(educationRepository.save(education));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeEducationResponse> getEducationsByEmployeeId(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
        return educationRepository.findByEmployeeIdOrderByStartYearDesc(employeeId).stream()
                .map(educationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeEducationResponse getEducationDetail(Long employeeId, Long id) {
        EmployeeEducation education = educationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!education.getEmployee().getId().equals(employeeId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return educationMapper.toResponse(education);
    }

    @Override
    @Transactional
    public EmployeeEducationResponse updateEducation(Long employeeId, Long id, UpdateEmployeeEducationRequest request) {
        EmployeeEducation education = educationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!education.getEmployee().getId().equals(employeeId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (request.getDegreeLevel() != null) education.setDegreeLevel(request.getDegreeLevel());
        if (request.getSchoolName() != null) education.setSchoolName(request.getSchoolName());
        if (request.getMajor() != null) education.setMajor(request.getMajor());
        if (request.getStartYear() != null) education.setStartYear(request.getStartYear());
        if (request.getEndYear() != null) education.setEndYear(request.getEndYear());
        if (request.getGpa() != null) education.setGpa(request.getGpa());
        if (request.getCertificateFileUrl() != null) education.setCertificateFileUrl(request.getCertificateFileUrl());
        if (request.getNote() != null) education.setNote(request.getNote());

        return educationMapper.toResponse(educationRepository.save(education));
    }

    @Override
    @Transactional
    public void deleteEducation(Long employeeId, Long id) {
        EmployeeEducation education = educationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!education.getEmployee().getId().equals(employeeId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        educationRepository.delete(education);
    }
}
