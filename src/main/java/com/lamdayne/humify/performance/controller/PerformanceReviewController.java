package com.lamdayne.humify.performance.controller;


import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.performance.dto.request.ReviewRequests;
import com.lamdayne.humify.performance.dto.response.ReviewResponse;
import com.lamdayne.humify.performance.service.PerformanceReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerformanceReviewController {

    private final PerformanceReviewService reviewService;

    // 1. Tạo mới bản đánh giá hiệu suất (Khởi tạo chu kỳ)
    @PostMapping("/employees/{employeeId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long employeeId,
            @Valid @RequestBody ReviewRequests.CreateReviewRequest request) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.REVIEW_CREATE_SUCCESS,
                        reviewService.createReview(employeeId, request)
                ));
    }

    // 2. Lấy danh sách lịch sử đánh giá của một nhân viên
    @GetMapping("/employees/{employeeId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByEmployeeId(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.REVIEW_READ_SUCCESS,
                        reviewService.getReviewsByEmployeeId(employeeId)
                ));
    }

    // 3. Xem chi tiết một bản đánh giá
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @PathVariable Long id) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.REVIEW_READ_SUCCESS,
                        reviewService.getReviewById(id)
                ));
    }

    // 4. Nhân viên tự đánh giá (SELF_REVIEW)
    @PutMapping("/reviews/{id}/self-score")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitSelfScore(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequests.SubmitSelfScoreRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.REVIEW_UPDATE_SUCCESS,
                        reviewService.submitSelfScore(id, request, userPrincipal.getId())
                ));
    }

    // 5. Quản lý đánh giá và nhận xét (MANAGER_REVIEW)
    @PutMapping("/reviews/{id}/reviewer-score")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReviewerScore(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequests.SubmitReviewerScoreRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.REVIEW_UPDATE_SUCCESS,
                        reviewService.submitReviewerScore(id, request, userPrincipal.getId())
                ));
    }

    // 6. Chốt điểm và đóng chu kỳ đánh giá (COMPLETED)
    @PutMapping("/reviews/{id}/complete")
    public ResponseEntity<ApiResponse<ReviewResponse>> completeReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequests.CompleteReviewRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.REVIEW_UPDATE_SUCCESS,
                        reviewService.completeReview(id, request, userPrincipal.getId())
                ));
    }
}
