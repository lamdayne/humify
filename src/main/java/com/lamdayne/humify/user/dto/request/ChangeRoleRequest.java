package com.lamdayne.humify.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class ChangeRoleRequest {

    @NotEmpty(message = "ROLE_ID_REQUIRED")
    private List<Long> roleIds;

}
