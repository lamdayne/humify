package com.lamdayne.humify.task.dto.response;

import com.lamdayne.humify.task.enums.TaskPriority;
import com.lamdayne.humify.task.enums.TaskType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TaskResponse {
    private Long id;
    private Long projectId;
    private String projectKey;
    private String taskKey;
    private Long sprintId;
    private Long columnId;
    private Long parentId;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskType type;
    private Integer points;
    private Double estimatedHours;
    private Double loggedHours;
    private Double position;
    private Instant dueDate;
    private Instant completedAt;
    private UserShortResponse reporter;
    private UserShortResponse assignee;
    private Instant createdAt;
    private Instant updatedAt;
}
