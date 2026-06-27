package com.lamdayne.humify.task.dto.request;

import com.lamdayne.humify.common.validator.ValidMoveTask;
import lombok.Getter;

@Getter
@ValidMoveTask
public class MoveTaskRequest {
    private Long columnId;
    private Long sprintId;
    private Long beforeTaskId;
    private Long afterTaskId;
}
