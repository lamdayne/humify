package com.lamdayne.humify.branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.io.Serializable;

@Getter
public class CreateBranchRequest implements Serializable {

    @NotNull(message = "COMPANY_ID_REQUIRED")
    private Long companyId;

    @NotBlank(message = "BRANCH_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "BRANCH_FIELD_REQUIRED")
    private String field;

    private String website;

    private String address;

    private Integer standardHoursPerDay;
}