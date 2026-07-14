package com.lamdayne.humify.payroll.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.payroll.dto.request.UpdatePayslipRequest;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.service.PayslipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payslips")
@RequiredArgsConstructor
public class PayslipController {

    private final PayslipService payslipService;

    /** PUT /payslips/{id} */
    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'PAYROLL_MANAGE')")
    public ResponseEntity<ApiResponse<PayslipResponse>> updatePayslip(
            @PathVariable("id") Long payslipId,
            @Valid @RequestBody UpdatePayslipRequest request

    ) {
        Long companyId = CompanyContext.getCompanyId();
        PayslipResponse data = payslipService.updatePayslip(payslipId, companyId, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                SuccessCode.PAYSLIP_UPDATE_SUCCESS,data));
    }
}
