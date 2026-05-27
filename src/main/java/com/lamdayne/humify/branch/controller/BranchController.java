package com.lamdayne.humify.branch.controller;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
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