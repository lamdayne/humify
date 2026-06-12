package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String... sorts);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void transferEmployee(Long id, TransferEmployeeRequest request);

    void updateEmployeeStatus(Long id, UpdateEmployeeStatusRequest request);

    void updateEmployeePosition(Long id, UpdateEmployeePositionRequest request);

    EmployeeResponse getByEmployeeCode(String employeeCode);

}
