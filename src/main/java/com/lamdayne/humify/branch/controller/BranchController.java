package com.lamdayne.humify.branch.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.request.UpdateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('BRANCH_CREATE', 'BRANCH_FULL')")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid CreateBranchRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.BRANCH_CREATE_SUCCESS,
                        branchService.createBranch(userPrincipal, request)
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'BRANCH_UPDATE', 'BRANCH_FULL')")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable Long id,
            @RequestBody @Valid UpdateBranchRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.COMPANY_UPDATE_SUCCESS,
                        branchService.updateBranch(id, request)
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'BRANCH_READ', 'BRANCH_FULL')")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.FOUND_BRANCH_SUCCESS,
                        branchService.getBranchResponseById(id)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'BRANCH_DELETE', 'BRANCH_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.COMPANY_DELETE_SUCCESS,
                        null
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'BRANCH_READ', 'BRANCH_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getAllBranches(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.FOUND_BRANCH_SUCCESS,
                        branchService.getAllBranches(page, size, sorts)
                )
        );
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'BRANCH_READ', 'BRANCH_FULL')")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranchesByCompanyId(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.FOUND_BRANCH_SUCCESS,
                        branchService.getBranchesByCompanyId(companyId)
                )
        );
    }
}