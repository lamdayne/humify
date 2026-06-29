package com.lamdayne.humify.performance.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.performance.dto.request.CreateKpiRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiProgressRequest;
import com.lamdayne.humify.performance.dto.response.KpiResponse;
import com.lamdayne.humify.performance.service.KpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class KpiController {

    private final KpiService kpiService;

    @PostMapping("/employees/{employeeId}/kpis")
    public ResponseEntity<ApiResponse<KpiResponse>> createKpi(
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateKpiRequest request
    ) {
        KpiResponse response = kpiService.createKpi(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.KPI_CREATE_SUCCESS, response));
    }

    @GetMapping("/employees/{employeeId}/kpis")
    public ResponseEntity<ApiResponse<List<KpiResponse>>> getKpis(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<KpiResponse> response = kpiService.getKpisByEmployee(employeeId, startDate, endDate);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.KPI_READ_SUCCESS, response));
    }

    @PutMapping("/kpis/{id}")
    public ResponseEntity<ApiResponse<KpiResponse>> updateKpi(
            @PathVariable Long id,
            @Valid @RequestBody CreateKpiRequest request
    ) {
        KpiResponse response = kpiService.updateKpi(id, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.KPI_UPDATE_SUCCESS, response));
    }

    @PutMapping("/kpis/{id}/progress")
    public ResponseEntity<ApiResponse<KpiResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKpiProgressRequest request
    ) {
        KpiResponse response = kpiService.updateProgress(id, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.KPI_PROGRESS_UPDATE_SUCCESS, response));
    }

    @DeleteMapping("/kpis/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteKpi(@PathVariable Long id) {
        kpiService.deleteKpi(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.KPI_DELETE_SUCCESS, null));
    }
}