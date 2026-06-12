package com.lamdayne.humify.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class EnumValueValidator implements ConstraintValidator<EnumValue, CharSequence> {

    private List<String> acceptedValues;

    @Override
    public void initialize(EnumValue enumValue) {
        acceptedValues = Arrays.stream(enumValue.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        return value == null || acceptedValues.contains(value.toString().toUpperCase());
    }
}
