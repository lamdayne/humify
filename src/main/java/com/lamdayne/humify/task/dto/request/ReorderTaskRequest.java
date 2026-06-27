package com.lamdayne.humify.task.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReorderTaskRequest {
    private Long beforeTaskId;
    private Long afterTaskId;
}
