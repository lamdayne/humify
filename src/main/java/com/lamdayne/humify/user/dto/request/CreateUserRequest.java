package com.lamdayne.humify.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class CreateUserRequest implements Serializable {

    private Long employeeId;

    @NotBlank(message = "USER_EMAIL_REQUIRED")
    private String email;

    @NotBlank(message = "USER_PASSWORD_REQUIRED")
    private String password;

    @NotEmpty(message = "ROLE_ID_REQUIRED")
    private List<Long> roleIds;

}
