package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeImportResponse;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.entity.Employee;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String... sorts);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void transferEmployee(Long id, TransferEmployeeRequest request);

    void updateEmployeeStatus(Long id, UpdateEmployeeStatusRequest request);

    void updateEmployeePosition(Long id, UpdateEmployeePositionRequest request);

    Employee getEmployeeByEmail(String email);

    EmployeeResponse getByEmployeeCode(String employeeCode);

    List<EmployeeImportResponse> importEmployeeFromXlsx(MultipartFile xlsxFile);

}
