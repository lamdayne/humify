package com.lamdayne.humify.project.dto.response;

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
public class RecentActivityResponse {
    private Long id;
    private Long taskId;
    private String taskKey;
    private String taskTitle;
    private String taskColumnName;
    private String userName;
    private String userAvatar;
    private String action;
    private String oldValue;
    private String newValue;
    private Instant createdAt;
}
