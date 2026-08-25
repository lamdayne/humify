package com.lamdayne.humify.performance.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.performance.dto.request.CreateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.response.KpiTemplateResponse;
import com.lamdayne.humify.performance.service.KpiTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi-templates")
public class KpiTemplateController {

    private final KpiTemplateService kpiTemplateService;

    @GetMapping
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_READ', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<List<KpiTemplateResponse>>> getAllTemplates(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<KpiTemplateResponse> response =
                kpiTemplateService.getAllTemplates(userPrincipal);

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.KPI_TEMPLATE_READ_SUCCESS,
                        response
                )
        );
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_READ', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<KpiTemplateResponse>> getTemplateById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        KpiTemplateResponse response =
                kpiTemplateService.getTemplateById(
                        userPrincipal,
                        id
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.KPI_TEMPLATE_READ_SUCCESS,
                        response
                )
        );
    }

    @PostMapping
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_CREATE', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<KpiTemplateResponse>> createTemplate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateKpiTemplateRequest request
    ) {
        KpiTemplateResponse response =
                kpiTemplateService.createTemplate(
                        userPrincipal,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                SuccessCode.KPI_TEMPLATE_CREATE_SUCCESS,
                                response
                        )
                );
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_UPDATE', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<KpiTemplateResponse>> updateTemplate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateKpiTemplateRequest request
    ) {
        KpiTemplateResponse response =
                kpiTemplateService.updateTemplate(
                        userPrincipal,
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.KPI_TEMPLATE_UPDATE_SUCCESS,
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_DELETE', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        kpiTemplateService.deleteTemplate(
                userPrincipal,
                id
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.KPI_TEMPLATE_DELETE_SUCCESS,
                        null
                )
        );
    }
}