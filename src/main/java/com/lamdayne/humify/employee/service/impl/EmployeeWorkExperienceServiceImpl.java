package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeWorkExperienceResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeWorkExperience;
import com.lamdayne.humify.employee.mapper.EmployeeWorkExperienceMapper;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.repository.EmployeeWorkExperienceRepository;
import com.lamdayne.humify.employee.service.EmployeeWorkExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeWorkExperienceServiceImpl implements EmployeeWorkExperienceService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkExperienceRepository employeeWorkExperienceRepository;
    private final EmployeeWorkExperienceMapper employeeWorkExperienceMapper;

    @Override
    @Transactional
    public EmployeeWorkExperienceResponse create(Long employeeId, CreateEmployeeWorkExperienceRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeWorkExperience experience = employeeWorkExperienceMapper.toEntity(request);
        experience.setEmployee(employee);

        employeeWorkExperienceRepository.save(experience);

        return employeeWorkExperienceMapper.toResponse(experience);
    }

    @Override
    public List<EmployeeWorkExperienceResponse> getAll(Long employeeId) {
        return employeeWorkExperienceRepository.findByEmployeeId(employeeId)
                .stream()
                .map(employeeWorkExperienceMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeWorkExperienceResponse getById(Long employeeId, Long id) {
        EmployeeWorkExperience experience = employeeWorkExperienceRepository
                .findByIdAndEmployeeId(id, employeeId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.EMPLOYEE_WORK_EXPERIENCE_NOT_FOUND));

        return employeeWorkExperienceMapper.toResponse(experience);
    }

    @Override
    @Transactional
    public EmployeeWorkExperienceResponse update(Long employeeId, Long id, UpdateEmployeeWorkExperienceRequest request) {
        EmployeeWorkExperience experience = employeeWorkExperienceRepository
                .findByIdAndEmployeeId(id, employeeId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.EMPLOYEE_WORK_EXPERIENCE_NOT_FOUND));

        employeeWorkExperienceMapper.update(experience, request);

        employeeWorkExperienceRepository.save(experience);

        return employeeWorkExperienceMapper.toResponse(experience);
    }

    @Override
    @Transactional
    public void delete(Long employeeId, Long id) {
        EmployeeWorkExperience experience = employeeWorkExperienceRepository
                .findByIdAndEmployeeId(id, employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_WORK_EXPERIENCE_NOT_FOUND));

        employeeWorkExperienceRepository.delete(experience);
    }
}
