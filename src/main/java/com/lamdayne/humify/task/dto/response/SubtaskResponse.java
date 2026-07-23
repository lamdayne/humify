package com.lamdayne.humify.task.dto.response;

import com.lamdayne.humify.task.enums.TaskPriority;
import com.lamdayne.humify.task.enums.TaskType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class SubtaskResponse {
    private Long id;
    private String taskKey;
    private String title;
    private TaskPriority priority;
    private TaskType type;
    private UserShortResponse assignee;
    private Long columnId;
    private Instant completedAt;
}
