package com.lamdayne.humify.task.dto.request;

import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.task.enums.TaskPriority;
import com.lamdayne.humify.task.enums.TaskType;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UpdateTaskRequest {

    @NotBlank(message = "TASK_TITLE_REQUIRED")
    private String title;

    private String description;

    @EnumValue(name = "priority", message = "TASK_PRIORITY_INVALID", enumClass = TaskPriority.class)
    private String priority;

    @EnumValue(name = "task_type", message = "TASK_TYPE_INVALID", enumClass = TaskType.class)
    private String type;

    @Min(value = 0, message = "TASK_POINTS_INVALID")
    private Integer points;

    @Positive(message = "TASK_ESTIMATED_HOURS_INVALID")
    private Double estimatedHours;

    @Future(message = "TASK_DUE_DATE_MUST_BE_FUTURE")
    private Instant dueDate;
}
