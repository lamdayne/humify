package com.lamdayne.humify.performance.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.performance.dto.request.CreatePerformanceReviewRequest;
import com.lamdayne.humify.performance.dto.request.ManagerReviewRequest;
import com.lamdayne.humify.performance.dto.request.SelfReviewRequest;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewSummaryResponse;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.performance.service.KpiCalculationService;
import com.lamdayne.humify.performance.service.PerformanceReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/performance-reviews")
public class PerformanceReviewController {

    private final PerformanceReviewService performanceReviewService;
    private final KpiCalculationService kpiCalculationService;
    @PostMapping
//    @PreAuthorize(
//            "hasAnyAuthority('FULL_ACCESS', 'KPI_CREATE', 'KPI_FULL')"
//    )
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> createReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreatePerformanceReviewRequest request
    ) {
        PerformanceReviewResponse response =
                performanceReviewService.createReview(
                        userPrincipal,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                SuccessCode.PERFORMANCE_REVIEW_CREATE_SUCCESS,
                                response
                        )
                );
    }

    @GetMapping
// @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_READ', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<PerformanceReviewResponse>>> getReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @RequestParam(required = false)
            Long employeeId,

            @RequestParam(required = false)
            PerformanceReviewStatus status,

            @RequestParam(required = false)
            LocalDate periodStart,

            @RequestParam(required = false)
            LocalDate periodEnd,

            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "PAGE_NO_INVALID")
            int page,

            @RequestParam(defaultValue = "10", required = false)
            @Min(value = 10, message = "PAGE_SIZE_INVALID")
            int size,

            @RequestParam(required = false)
            String... sorts
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                SuccessCode.PERFORMANCE_REVIEW_READ_SUCCESS,
                                performanceReviewService.getReviews(
                                        userPrincipal,
                                        employeeId,
                                        status,
                                        periodStart,
                                        periodEnd,
                                        page,
                                        size,
                                        sorts
                                )
                        )
                );
    }

    @GetMapping("/{id}")
//    @PreAuthorize(
//            "hasAnyAuthority('FULL_ACCESS', 'KPI_READ', 'KPI_FULL')"
//    )
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> getReviewById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        PerformanceReviewResponse response =
                performanceReviewService.getReviewById(
                        userPrincipal,
                        id
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_READ_SUCCESS,
                        response
                )
        );
    }
    @PostMapping("/{id}/refresh-kpis")
//    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'KPI_UPDATE', 'KPI_FULL')")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> refreshKpis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        PerformanceReviewResponse response =
                kpiCalculationService.refreshReviewKpis(
                        userPrincipal,
                        id
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.KPI_CALCULATE_SUCCESS,
                        response
                )
        );
    }

    @PutMapping("/{id}/self-review")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> selfReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody SelfReviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_SELF_REVIEW_SUCCESS,
                        performanceReviewService.selfReview(
                                userPrincipal,
                                id,
                                request
                        )
                )
        );
    }

    @PutMapping("/{id}/manager-review")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> managerReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody ManagerReviewRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_MANAGER_REVIEW_SUCCESS,
                        performanceReviewService.managerReview(
                                userPrincipal,
                                id,
                                request
                        )
                )
        );
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<PerformanceReviewResponse>> completeReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_COMPLETE_SUCCESS,
                        performanceReviewService.completeReview(
                                userPrincipal,
                                id
                        )
                )
        );
    }

    //nhân viên tự xem review của mình
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<PerformanceReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @RequestParam(required = false)
            PerformanceReviewStatus status,

            @RequestParam(required = false)
            LocalDate periodStart,

            @RequestParam(required = false)
            LocalDate periodEnd,

            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "PAGE_NO_INVALID")
            int page,

            @RequestParam(defaultValue = "10", required = false)
            @Min(value = 10, message = "PAGE_SIZE_INVALID")
            int size,

            @RequestParam(required = false)
            String... sorts
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_READ_SUCCESS,
                        performanceReviewService.getMyReviews(
                                userPrincipal,
                                status,
                                periodStart,
                                periodEnd,
                                page,
                                size,
                                sorts
                        )
                )
        );
    }

    //
    @GetMapping("/reviewer/me")
    public ResponseEntity<ApiResponse<PageResponse<PerformanceReviewResponse>>> getMyAssignedReviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @RequestParam(required = false)
            Long employeeId,

            @RequestParam(required = false)
            PerformanceReviewStatus status,

            @RequestParam(required = false)
            LocalDate periodStart,

            @RequestParam(required = false)
            LocalDate periodEnd,

            @RequestParam(defaultValue = "0", required = false)
            @Min(value = 0, message = "PAGE_NO_INVALID")
            int page,

            @RequestParam(defaultValue = "10", required = false)
            @Min(value = 10, message = "PAGE_SIZE_INVALID")
            int size,

            @RequestParam(required = false)
            String... sorts
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_READ_SUCCESS,
                        performanceReviewService.getMyAssignedReviews(
                                userPrincipal,
                                employeeId,
                                status,
                                periodStart,
                                periodEnd,
                                page,
                                size,
                                sorts
                        )
                )
        );
    }

    @GetMapping("/reviewer/me/summary")
    public ResponseEntity<
            ApiResponse<PerformanceReviewSummaryResponse>
            > getMyAssignedReviewSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_READ_SUCCESS,
                        performanceReviewService
                                .getMyAssignedReviewSummary(
                                        userPrincipal
                                )
                )
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        performanceReviewService.deleteReview(
                userPrincipal,
                id
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        SuccessCode.PERFORMANCE_REVIEW_DELETE_SUCCESS,
                        null
                )
        );
    }
}