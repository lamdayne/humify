package com.lamdayne.humify.employee.dto.request;

import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class UpdateEmployeeStatusRequest implements Serializable {

    @NotNull(message = "EMPLOYEE_STATUS_REQUIRED")
    @EnumValue(name = "status", message = "EMPLOYEE_STATUS_INVALID", enumClass = EmployeeStatus.class)
    private String status;

}
