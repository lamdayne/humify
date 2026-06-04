package com.lamdayne.humify.company.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.request.UpdateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> registry(@RequestBody @Valid CreateCompanyRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.COMPANY_CREATE_SUCCESS, companyService.createCompany(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'COMPANY_READ')")
    public ResponseEntity<ApiResponse<PageResponse<CompanyResponse>>> getAllCompanies(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.COMPANY_READ_SUCCESS,
                        companyService.getAllCompanies(page, size, sorts)
                ));
    }

    @PostMapping("/{companyCode}/approve")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public ResponseEntity<ApiResponse<?>> approveCompany(
            @PathVariable(name = "companyCode") String companyCode
    ) {
        companyService.approveCompany(companyCode);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.APPROVE_COMPANY_SUCCESS));
    }

    @PutMapping("/{companyCode}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'COMPANY_UPDATE')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @RequestBody @Valid UpdateCompanyRequest request,
            @PathVariable(name = "companyCode") String companyCode
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.COMPANY_UPDATE_SUCCESS,
                        "Update company info successfully",
                        companyService.updateCompany(companyCode, request)
                ));
    }

}
