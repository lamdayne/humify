package com.lamdayne.humify.payroll.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.payroll.dto.request.CreatePayrollPeriodRequest;
import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;
import com.lamdayne.humify.payroll.service.PayrollPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payroll-periods")
public class PayrollPeriodController {

    private final PayrollPeriodService payrollPeriodService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PAYROLL_PERIOD_CREATE')")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> createPayrollPeriod(
            @Valid @RequestBody CreatePayrollPeriodRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.PAYROLL_PERIOD_CREATE_SUCCESS,
                        payrollPeriodService.createPayrollPeriod(userPrincipal.getCompanyId(), request)
                ));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<PayrollPeriodResponse>>> getPayrollPeriods(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.PAYROLL_PERIOD_READ_SUCCESS,
                        payrollPeriodService.getPayrollPeriods(userPrincipal.getCompanyId(), page, size)
                ));
    }
}
