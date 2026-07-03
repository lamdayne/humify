package com.lamdayne.humify.task.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AssignTaskRequest {

    @NotNull(message = "TASK_ASSIGNEE_ID_REQUIRED")
    private Long assigneeId;

}
