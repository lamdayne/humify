package com.lamdayne.humify.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ResetPasswordRequest implements Serializable {

    @NotBlank(message = "RESET_TOKEN_REQUIRED")
    private String token;

    @NotBlank(message = "PASSWORD_REQUIRED")
    private String newPassword;

    @NotBlank(message = "CONFIRM_PASSWORD_REQUIRED")
    private String confirmNewPassword;

}
