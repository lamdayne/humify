package com.lamdayne.humify.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateWorklogRequest {

    @NotNull
    @Positive
    private Double timeSpentHours;

    private String description;

    @NotNull
    private Instant loggedAt;
}