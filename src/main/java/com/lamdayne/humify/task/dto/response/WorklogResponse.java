package com.lamdayne.humify.task.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class WorklogResponse {

    private Long id;

    private Long taskId;

    private UserSummaryResponse user;

    private Double timeSpentHours;

    private String description;

    private Instant loggedAt;

    private Instant createdAt;
}

