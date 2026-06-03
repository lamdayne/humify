package com.lamdayne.humify.position.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.position.dto.request.PositionRequest;
import com.lamdayne.humify.position.dto.response.PositionResponse;
import com.lamdayne.humify.position.service.PositionService;
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
@RequestMapping("positions")
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(
            @RequestBody @Valid PositionRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.POSITION_CREATE_SUCCESS,
                        positionService.createPosition(request)
                ));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositionByCompanyId(
            @PathVariable Long companyId
    ) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.FOUND_POSITION_SUCCESS,
                        positionService.getPositionByCompanyId(companyId)
                ));
    }
}