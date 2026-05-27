package com.lamdayne.humify.branch.controller;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(@RequestBody @Valid CreateBranchRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.BRANCH_CREATE_SUCCESS, branchService.createBranch(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.FOUND_BRANCH_SUCCESS,
                        branchService.getBranchResponseById(id)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getAllBranches(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.FOUND_BRANCH_SUCCESS,
                        branchService.getAllBranches(page, size, sort)
                )
        );
    }

    @GetMapping("/company/{companyId}")
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