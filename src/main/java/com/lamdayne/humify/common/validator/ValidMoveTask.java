package com.lamdayne.humify.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MoveTaskValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMoveTask {

    String message() default "INVALID_MOVE_TASK";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
