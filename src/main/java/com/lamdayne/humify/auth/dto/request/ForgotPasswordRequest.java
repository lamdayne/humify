package com.lamdayne.humify.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ForgotPasswordRequest implements Serializable {

    @NotBlank(message = "EMAIL_REQUIRED")
    private String email;

}
