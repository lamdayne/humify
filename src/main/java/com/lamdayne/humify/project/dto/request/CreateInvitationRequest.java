package com.lamdayne.humify.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInvitationRequest {

    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotNull(message = "PROJECT_ROLE_NOT_FOUND")
    private Long projectRoleId;

    private Long ttlMinutes = 10080L;
}
