package com.lamdayne.humify.performance.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReviewSummaryResponse {

    private long totalReviews;

    private long awaitingSelfReview;

    private long awaitingManagerReview;

    private long awaitingCompletion;

    private long completed;
}
