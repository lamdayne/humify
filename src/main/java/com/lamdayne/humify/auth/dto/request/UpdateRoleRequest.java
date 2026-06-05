package com.lamdayne.humify.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class UpdateRoleRequest implements Serializable {

    @NotBlank(message = "ROLE_NAME_REQUIRED")
    private String name;

    private String description;

    @NotEmpty(message = "ROLE_PERMISSIONS_REQUIRED")
    private List<Long> permissionIds;

}
