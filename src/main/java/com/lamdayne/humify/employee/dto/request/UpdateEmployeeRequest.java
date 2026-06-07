package com.lamdayne.humify.employee.dto.request;

import com.lamdayne.humify.common.validator.EmailPattern;
import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.employee.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
public class UpdateEmployeeRequest implements Serializable {

    @NotBlank(message = "EMPLOYEE_FULL_NAME_REQUIRED")
    private String fullName;

    private LocalDate dateOfBirth;

    @NotBlank(message = "EMPLOYEE_GENDER_REQUIRED")
    @EnumValue(name = "gender", message = "EMPLOYEE_GENDER_INVALID", enumClass = Gender.class)
    private String gender;

    @NotBlank(message = "EMPLOYEE_EMAIL_REQUIRED")
    @EmailPattern(message = "EMPLOYEE_EMAIL_INVALID")
    private String email;

    private String phone;

    private String address;

    private LocalDate startDate;

}
