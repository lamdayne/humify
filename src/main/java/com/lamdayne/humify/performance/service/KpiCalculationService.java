package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.entity.PerformanceReview;

public interface KpiCalculationService {
    PerformanceReviewResponse refreshReviewKpis(
            UserPrincipal userPrincipal,
            Long reviewId
    );
    void calculateReviewKpis(
            PerformanceReview review
    );
}
