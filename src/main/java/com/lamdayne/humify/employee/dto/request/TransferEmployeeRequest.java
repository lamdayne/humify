package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class TransferEmployeeRequest implements Serializable {

    @NotNull(message = "BRANCH_ID_REQUIRED")
    @Positive(message = "BRANCH_ID_INVALID")
    private Long branchId;

    @NotNull(message = "DEPARTMENT_ID_REQUIRED")
    @Positive(message = "DEPARTMENT_ID_INVALID")
    private Long departmentId;

}
