package com.lamdayne.humify.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class CreateUserRequest implements Serializable {

    private Long employeeId;

    @NotBlank(message = "USER_EMAIL_REQUIRED")
    private String email;

    @NotBlank(message = "USER_PASSWORD_REQUIRED")
    private String password;

}
