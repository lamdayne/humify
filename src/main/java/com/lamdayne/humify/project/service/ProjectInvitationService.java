package com.lamdayne.humify.project.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.project.dto.request.AcceptInvitationRequest;
import com.lamdayne.humify.project.dto.request.CreateInvitationRequest;
import com.lamdayne.humify.project.dto.response.InvitationResponse;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.dto.response.ValidateInvitationResponse;

public interface ProjectInvitationService {

    InvitationResponse createInvitation(Long projectId, UserPrincipal userPrincipal, CreateInvitationRequest request);

    ValidateInvitationResponse validateInvitation(String token);

    ProjectMemberResponse acceptInvitation(UserPrincipal userPrincipal, AcceptInvitationRequest request);
}