package com.lamdayne.humify.project.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private Long id;
    private Long projectId;
    private Long projectRoleId;
    private String email;
    private String token;
    private String inviteLink;
    private Long inviterId;
    private String status;
    private Instant expiresAt;
    private Instant createdAt;
}
