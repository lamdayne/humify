package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.employee.dto.request.CreateEmployeeEducationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeEducationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeEducationResponse;

import java.util.List;

public interface EmployeeEducationService {

    EmployeeEducationResponse createEducation(Long employeeId, CreateEmployeeEducationRequest request);
    List<EmployeeEducationResponse> getEducationsByEmployeeId(Long employeeId);
    EmployeeEducationResponse getEducationDetail(Long employeeId, Long id);
    EmployeeEducationResponse updateEducation(Long employeeId, Long id, UpdateEmployeeEducationRequest request);
    void deleteEducation(Long employeeId, Long id);
}
