package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.performance.dto.request.ReviewRequests;
import com.lamdayne.humify.performance.dto.response.ReviewResponse;

import java.util.List;

public interface PerformanceReviewService {

    ReviewResponse createReview(Long employeeId, ReviewRequests.CreateReviewRequest request);
    List<ReviewResponse> getReviewsByEmployeeId(Long employeeId);
    ReviewResponse getReviewById(Long id);
    ReviewResponse submitSelfScore(Long id, ReviewRequests.SubmitSelfScoreRequest request, Long currentUserId);
    ReviewResponse submitReviewerScore(Long id, ReviewRequests.SubmitReviewerScoreRequest request, Long currentUserId);
    ReviewResponse completeReview(Long id, ReviewRequests.CompleteReviewRequest request, Long currentUserId);
}
