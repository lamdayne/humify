package com.lamdayne.humify.task.enums;

import lombok.Getter;

@Getter
public enum TaskActivityAction {
    CREATE_TASK,
    UPDATE_STATUS,
    CHANGE_ASSIGNEE,
    UPDATE_PRIORITY,
    CHANGE_SPRINT,
    UPDATE_POINTS,
    ADD_COMMENT,
    ADD_ATTACHMENT
}
