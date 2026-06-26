package com.lamdayne.humify.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInvitationRequest {
    private String email;

    @NotNull(message = "ROLE_ID_REQUIRED")
    private Long projectRoleId;

    private Long ttlMinutes = 10080L;
}
