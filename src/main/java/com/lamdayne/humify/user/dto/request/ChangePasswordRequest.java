package com.lamdayne.humify.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "USER_OLD_PASSWORD_REQUIRED")
    private String oldPassword;

    @NotBlank(message = "USER_PASSWORD_REQUIRED")
    private String newPassword;

}

