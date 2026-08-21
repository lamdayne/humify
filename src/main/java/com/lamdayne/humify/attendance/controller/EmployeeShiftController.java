package com.lamdayne.humify.attendance.controller;

import com.lamdayne.humify.attendance.dto.request.CreateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.response.EmployeeShiftResponse;
import com.lamdayne.humify.attendance.service.EmployeeShiftService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.common.util.PageableUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/employee-shifts")
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'WORK_SHIFT_CREATE', 'WORK_SHIFT_FULL')")
    public ResponseEntity<ApiResponse<EmployeeShiftResponse>> assignShift(
            @Valid @RequestBody CreateEmployeeShiftRequest request
    ) {
        EmployeeShiftResponse response = employeeShiftService.assignShift(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_SHIFT_CREATE_SUCCESS, response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'WORK_SHIFT_READ', 'WORK_SHIFT_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeShiftResponse>>> getEmployeeShifts(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(required = false) String[] sorts,
            @RequestParam(required = false, name = "search") String[] searchParams
    ) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        PageResponse<EmployeeShiftResponse> response = employeeShiftService.getEmployeeShifts(pageable, searchParams);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_SHIFT_READ_SUCCESS, response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'WORK_SHIFT_READ', 'WORK_SHIFT_FULL')")
    public ResponseEntity<ApiResponse<EmployeeShiftResponse>> getEmployeeShiftDetail(@PathVariable Long id) {
        EmployeeShiftResponse response = employeeShiftService.getEmployeeShiftDetail(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_SHIFT_READ_SUCCESS, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'WORK_SHIFT_UPDATE', 'WORK_SHIFT_FULL')")
    public ResponseEntity<ApiResponse<EmployeeShiftResponse>> updateEmployeeShift(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeShiftRequest request
    ) {
        EmployeeShiftResponse response = employeeShiftService.updateEmployeeShift(id, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_SHIFT_UPDATE_SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'WORK_SHIFT_DELETE', 'WORK_SHIFT_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployeeShift(@PathVariable Long id) {
        employeeShiftService.deleteEmployeeShift(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_SHIFT_DELETE_SUCCESS, null));
    }
}
