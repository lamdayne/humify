package com.lamdayne.humify.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long id;
    private Long taskId;

    private ActivityUserResponse user;

    private String action;

    private String oldValue;

    private String newValue;

    private Instant createdAt;

}