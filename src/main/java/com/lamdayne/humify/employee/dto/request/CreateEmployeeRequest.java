package com.lamdayne.humify.employee.dto.request;

import com.lamdayne.humify.common.validator.EmailPattern;
import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class CreateEmployeeRequest {

    @NotNull(message = "BRANCH_ID_REQUIRED")
    @Positive(message = "BRANCH_ID_INVALID")
    private Long branchId;

    @NotNull(message = "DEPARTMENT_ID_REQUIRED")
    @Positive(message = "DEPARTMENT_ID_INVALID")
    private Long departmentId;

    @NotNull(message = "POSITION_ID_REQUIRED")
    @Positive(message = "POSITION_ID_INVALID")
    private Long positionId;

    @NotBlank(message = "EMPLOYEE_FULL_NAME_REQUIRED")
    private String fullName;

    private String avatarUrl;

    private LocalDate dateOfBirth;

    @NotNull(message = "EMPLOYEE_GENDER_REQUIRED")
    @EnumValue(name = "gender", message = "EMPLOYEE_GENDER_INVALID", enumClass = Gender.class)
    private String gender;

    @NotBlank(message = "EMPLOYEE_EMAIL_REQUIRED")
    @EmailPattern(message = "EMPLOYEE_EMAIL_INVALID")
    private String email;

    private String phone;

    private String address;

    @NotNull(message = "EMPLOYEE_START_DATE_REQUIRED")
    private LocalDate startDate;

    @NotNull(message = "EMPLOYEE_STATUS_REQUIRED")
    @EnumValue(name = "status", message = "EMPLOYEE_STATUS_INVALID", enumClass = EmployeeStatus.class)
    private String status;

    @NotEmpty(message = "ROLE_ID_REQUIRED")
    private List<Long> roleIds;

}
