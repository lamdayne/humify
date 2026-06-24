package com.lamdayne.humify.project.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ProjectMemberResponse {
    private Long id;
    private Long projectId;
    private MemberInfo user;
    private ProjectRoleResponse role;
    private String status;
    private String invitedEmail;
    private Instant joinedAt;
}
