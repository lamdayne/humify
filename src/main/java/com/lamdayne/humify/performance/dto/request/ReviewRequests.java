package com.lamdayne.humify.performance.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class ReviewRequests {

    @Getter
    public static class CreateReviewRequest {
        @NotBlank(message = "REVIEW_PERIOD_REQUIRED")
        private String reviewPeriod;

        @NotNull(message = "REVIEWER_ID_REQUIRED")
        private Long reviewerId;
    }

    @Getter
    public static class SubmitSelfScoreRequest {
        @NotNull(message = "SCORE_REQUIRED")
        @Min(value = 1, message = "SCORE_MIN_INVALID")
        @Max(value = 5, message = "SCORE_MAX_INVALID")
        private Double selfScore;
    }

    @Getter
    public static class SubmitReviewerScoreRequest {
        @NotNull(message = "SCORE_REQUIRED")
        @Min(value = 1, message = "SCORE_MIN_INVALID")
        @Max(value = 5, message = "SCORE_MAX_INVALID")
        private Double reviewerScore;

        private String feedback;
    }

    @Getter
    public static class CompleteReviewRequest {
        @NotNull(message = "SCORE_REQUIRED")
        @Min(value = 1, message = "SCORE_MIN_INVALID")
        @Max(value = 5, message = "SCORE_MAX_INVALID")
        private Double finalScore;
    }
}
