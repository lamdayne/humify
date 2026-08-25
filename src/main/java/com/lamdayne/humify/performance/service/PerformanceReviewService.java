package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.performance.dto.request.CreatePerformanceReviewRequest;
import com.lamdayne.humify.performance.dto.request.ManagerReviewRequest;
import com.lamdayne.humify.performance.dto.request.ReviewRequests;
import com.lamdayne.humify.performance.dto.request.SelfReviewRequest;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewSummaryResponse;
import com.lamdayne.humify.performance.dto.response.ReviewResponse;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;

import java.time.LocalDate;
import java.util.List;

public interface PerformanceReviewService {


    PerformanceReviewResponse createReview(UserPrincipal userPrincipal, CreatePerformanceReviewRequest request);

    PerformanceReviewResponse getReviewById(UserPrincipal userPrincipal, Long reviewId);

    PageResponse<PerformanceReviewResponse> getReviews(
            UserPrincipal userPrincipal,
            Long employeeId,
            PerformanceReviewStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            int page,
            int size,
            String... sorts
    );

    PageResponse<PerformanceReviewResponse> getMyReviews(
            UserPrincipal userPrincipal,
            PerformanceReviewStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            int page,
            int size,
            String... sorts
    );
    PageResponse<PerformanceReviewResponse> getMyAssignedReviews(
            UserPrincipal userPrincipal,
            Long employeeId,
            PerformanceReviewStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            int page,
            int size,
            String... sorts
    );
    PerformanceReviewResponse selfReview(UserPrincipal userPrincipal, Long reviewId, SelfReviewRequest request);

    PerformanceReviewResponse managerReview(UserPrincipal userPrincipal, Long reviewId, ManagerReviewRequest request);

    PerformanceReviewResponse completeReview(UserPrincipal userPrincipal, Long reviewId);

    PerformanceReviewSummaryResponse getMyAssignedReviewSummary(
            UserPrincipal userPrincipal
    );
}
