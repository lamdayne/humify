package com.lamdayne.humify.performance.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfReviewRequest {

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double selfScore;
}