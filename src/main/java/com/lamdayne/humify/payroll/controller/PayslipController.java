package com.lamdayne.humify.payroll.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.payroll.dto.request.UpdatePayslipRequest;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.service.PayslipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAYSLIP_UPDATE')")
    public ResponseEntity<ApiResponse<PayslipResponse>> updatePayslip(
            @PathVariable("id") Long payslipId,
            @Valid @RequestBody UpdatePayslipRequest request

    ) {
        PayslipResponse data = payslipService.updatePayslip(payslipId, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                SuccessCode.PAYSLIP_UPDATE_SUCCESS,data));
    }
}
