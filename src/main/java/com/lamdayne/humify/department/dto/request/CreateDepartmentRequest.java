package com.lamdayne.humify.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.io.Serializable;
@Getter
public class CreateDepartmentRequest implements Serializable {
    @NotNull(message = "BRANCH_ID_REQUIRED")
    private Long branchId;

    @NotBlank(message = "DEPARTMENT_NAME_REQUIRED")
    private String name;

    private String description;


}
