package com.lamdayne.humify.common.validator;

import com.lamdayne.humify.task.dto.request.MoveTaskRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MoveTaskValidator implements ConstraintValidator<ValidMoveTask, MoveTaskRequest> {

    @Override
    public boolean isValid(MoveTaskRequest moveTaskRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (moveTaskRequest == null) return true;

        return moveTaskRequest.getColumnId() != null || moveTaskRequest.getSprintId() != null;
    }
}
