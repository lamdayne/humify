package com.lamdayne.humify.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmailPattern {
    String message() default "Invalid Email Address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
