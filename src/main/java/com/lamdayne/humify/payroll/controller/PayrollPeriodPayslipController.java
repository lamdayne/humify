package com.lamdayne.humify.payroll.controller;


import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.service.PayrollPeriodService;
import com.lamdayne.humify.payroll.service.PayslipService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payroll-periods")
@RequiredArgsConstructor
public class PayrollPeriodPayslipController {

    private final PayrollPeriodService payrollPeriodService;
    private final PayslipService payslipService;

    /** POST /payroll-periods/{id}/calculate */
        @PostMapping("/{id}/calculates")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'PAYROLL_MANAGE')")
    public ResponseEntity<ApiResponse<Integer>> calculate(@PathVariable("id") Long payrollPeriodId) {
        int count = payrollPeriodService.calculate(payrollPeriodId);
        return ResponseEntity.ok()
                .body(ApiResponse
                        .success(SuccessCode.PAYROLL_PERIOD_CALC_SUCCESS,count));
    }

    /** GET /payroll-periods/{id}/payslips */
    @GetMapping("/{id}/payslips")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'PAYROLL_MANAGE')")
    public ResponseEntity<ApiResponse<PageResponse<PayslipResponse>>> getPayslips(
            @PathVariable("id") Long payrollPeriodId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) PayslipStatus status,
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {

        PageResponse<PayslipResponse> data = payslipService.getPayslipsByPeriod(
                payrollPeriodId, employeeId, status, page, size, sorts
        );
        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.PAYSLIP_READ_SUCCESS, data)
        );
    }

    /** PUT /payroll-periods/{id}/approve */
    @PutMapping("/{id}/approve")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'PAYROLL_APPROVE')")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable("id") Long payrollPeriodId) {

        payrollPeriodService.approve(payrollPeriodId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PAYROLL_PERIOD_APPROVE_SUCCESS,
                        null
                ));
    }

    /** PUT /payroll-periods/{id}/pay */
    @PutMapping("/{id}/pay")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'PAYROLL_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> pay(@PathVariable("id") Long payrollPeriodId) {
        payrollPeriodService.pay(payrollPeriodId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.ROLL_PERIOD_PAY_SUCCESS,null));
    }
}
