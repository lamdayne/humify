package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.employee.dto.request.CreateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeWorkExperienceResponse;

import java.util.List;

public interface EmployeeWorkExperienceService {

    EmployeeWorkExperienceResponse create(Long employeeId, CreateEmployeeWorkExperienceRequest request);

    List<EmployeeWorkExperienceResponse> getAll(Long employeeId);

    EmployeeWorkExperienceResponse getById(Long employeeId, Long id);

    EmployeeWorkExperienceResponse update(Long employeeId, Long id, UpdateEmployeeWorkExperienceRequest request);

    void delete(Long employeeId, Long id);

}
