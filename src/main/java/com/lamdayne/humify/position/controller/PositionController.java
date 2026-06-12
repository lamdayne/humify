package com.lamdayne.humify.position.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.position.dto.request.CreatePositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.service.PositionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/positions")
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'POSITION_CREATE', 'POSITION_FULL')")
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid CreatePositionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.POSITION_CREATE_SUCCESS,
                        positionService.createPosition(userPrincipal, request)
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'POSITION_READ', 'POSITION_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<PositionResponse>>> getAllPositions(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.FOUND_POSITION_SUCCESS,
                        positionService.getAllPositions(page, size, sorts)
                ));
    }
}