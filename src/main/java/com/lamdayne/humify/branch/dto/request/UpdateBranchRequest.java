package com.lamdayne.humify.branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
public class UpdateBranchRequest implements Serializable{
    @NotBlank(message = "BRANCH_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "BRANCH_FIELD_REQUIRED")
    private String field;

    private String website;
    private String address;
    private Integer standardHoursPerDay;
    private String status;
}
