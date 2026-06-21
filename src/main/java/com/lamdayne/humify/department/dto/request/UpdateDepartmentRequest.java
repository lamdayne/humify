package com.lamdayne.humify.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpdateDepartmentRequest {
    @NotNull(message = "BRANCH_ID_REQUIRED")
    private Long branchId;

    @NotBlank(message = "DEPARTMENT_NAME_REQUIRED")
    private String name;

    private String description;
}
