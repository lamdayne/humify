package com.lamdayne.humify.department.controller;


import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;

import com.lamdayne.humify.department.dto.request.CreateDepartmentRequest;
import com.lamdayne.humify.department.dto.response.DepartmentResponse;
import com.lamdayne.humify.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>>createDepartment(@RequestBody @Valid CreateDepartmentRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.DEPARTMENT_CREATE_SUCCESS,departmentService.createDepartment(request)));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> findByBranchId(@PathVariable Long branchId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.FOUND_DEPARTMENT_SUCCESS,departmentService.getDepartmentByBranchId(branchId)));
    }

}
