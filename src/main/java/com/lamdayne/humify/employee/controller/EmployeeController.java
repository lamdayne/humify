package com.lamdayne.humify.employee.controller;

import com.lamdayne.humify.common.excel.dto.ImportResult;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.employee.service.impl.EmployeeExcelImportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_CREATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @RequestBody @Valid CreateEmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_CREATE_SUCCESS,
                        employeeService.createEmployee(request)
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_READ', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_READ_SUCCESS,
                        employeeService.getAllEmployees(page, size, sorts)
                ));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid UpdateEmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_UPDATE_SUCCESS,
                        "Update info employee successfully",
                        employeeService.updateEmployee(employeeId, request)
                ));
    }

    @PutMapping("/{employeeId}/transfer")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> transferEmployee(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid TransferEmployeeRequest request
    ) {
        employeeService.transferEmployee(employeeId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_TRANSFER_SUCCESS
                ));
    }

    @PutMapping("/{employeeId}/status")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> updateEmployeeStatus(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid UpdateEmployeeStatusRequest request
    ) {
        employeeService.updateEmployeeStatus(employeeId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_STATUS_UPDATE_SUCCESS
                ));
    }

    @PutMapping("/{employeeId}/position")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> updateEmployeePosition(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid UpdateEmployeePositionRequest request
    ) {
        employeeService.updateEmployeePosition(employeeId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_POSITION_UPDATE_SUCCESS
                ));
    }
    private final EmployeeExcelImportService employeeExcelImportService;

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ImportResult>> importExcel(@RequestParam MultipartFile file) {
        ImportResult result = employeeExcelImportService.importExcel(file);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.EMPLOYEE_IMPORT_SUCCESS, result));
    }

}
