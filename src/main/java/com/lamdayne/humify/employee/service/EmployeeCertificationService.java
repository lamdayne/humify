package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeCertificationResponse;

public interface EmployeeCertificationService {

    EmployeeCertificationResponse addEmployeeCertification(Long employeeId, CreateEmployeeCertificationRequest request);

    EmployeeCertificationResponse updateEmployeeCertification(
            UserPrincipal userPrincipal,
            Long employeeId,
            Long id,
            UpdateEmployeeCertificationRequest request
    );

    EmployeeCertificationResponse getEmployeeCertification(UserPrincipal userPrincipal, Long employeeId, Long id);

    void deleteEmployeeCertification(Long employeeId, Long id);

    PageResponse<EmployeeCertificationResponse> getEmployeeCertifications(Long employeeId, int page, int size, String... sorts);

}
