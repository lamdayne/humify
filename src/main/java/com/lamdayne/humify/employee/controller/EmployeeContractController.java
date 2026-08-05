package com.lamdayne.humify.employee.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.employee.dto.request.CreateContractRequest;
import com.lamdayne.humify.employee.dto.request.UpdateContractRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeContractResponse;
import com.lamdayne.humify.employee.enums.ContractStatus;
import com.lamdayne.humify.employee.service.EmployeeContractService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/employee-contracts")
public class EmployeeContractController {

    private final EmployeeContractService contractService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CONTRACT_READ', 'CONTRACT_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeContractResponse>>> getContracts(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        PageResponse<EmployeeContractResponse> response = contractService.getContracts(employeeId, status, page, size, sorts);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.CONTRACT_READ_SUCCESS, response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CONTRACT_READ', 'CONTRACT_FULL')")
    public ResponseEntity<ApiResponse<EmployeeContractResponse>> getContractById(
            @PathVariable Long id
    ) {
        EmployeeContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.CONTRACT_READ_SUCCESS, response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CONTRACT_CREATE', 'CONTRACT_FULL')")
    public ResponseEntity<ApiResponse<EmployeeContractResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request
    ) {
        EmployeeContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.CONTRACT_CREATE_SUCCESS, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CONTRACT_UPDATE', 'CONTRACT_FULL')")
    public ResponseEntity<ApiResponse<EmployeeContractResponse>> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContractRequest request
    ) {
        EmployeeContractResponse response = contractService.updateContract(id, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.CONTRACT_UPDATE_SUCCESS, response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'CONTRACT_DELETE', 'CONTRACT_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.CONTRACT_DELETE_SUCCESS, null));
    }
}