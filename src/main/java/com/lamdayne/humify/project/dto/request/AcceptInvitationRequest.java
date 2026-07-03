package com.lamdayne.humify.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AcceptInvitationRequest implements Serializable {

    @NotBlank(message = "INVITATION_TOKEN_REQUIRED")
    private String token;
}