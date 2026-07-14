package com.lamdayne.humify.payroll.controller;


import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.payroll.dto.response.MyPayslipResponse;
import com.lamdayne.humify.payroll.service.PayslipService;
import com.lamdayne.humify.user.service.UserService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeePayslipController {

    private final PayslipService payslipService;
    private final UserService userService;


    /** GET /employees/my-payslips */
    @GetMapping("/my-payslips")
    public ResponseEntity<ApiResponse<PageResponse<MyPayslipResponse>>> getMyPayslips(
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts

    ) {
        Long companyId = CompanyContext.getCompanyId();
        Long employeeId = userService.getCurrentEmployeeId();
        PageResponse<MyPayslipResponse> data = payslipService.getMyPayslips(employeeId, companyId, year, page, size, sorts );
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.PAYSLIP_READ_SUCCESS, data));
    }
}
