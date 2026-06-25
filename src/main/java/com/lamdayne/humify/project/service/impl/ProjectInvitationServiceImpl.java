package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.project.dto.request.AcceptInvitationRequest;
import com.lamdayne.humify.project.dto.request.CreateInvitationRequest;
import com.lamdayne.humify.project.dto.response.InvitationResponse;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.dto.response.ValidateInvitationResponse;
import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.entity.ProjectInvitation;
import com.lamdayne.humify.project.entity.ProjectMember;
import com.lamdayne.humify.project.entity.ProjectRole;
import com.lamdayne.humify.project.enums.ProjectInvitationStatus;
import com.lamdayne.humify.project.enums.ProjectMemberStatus;
import com.lamdayne.humify.project.mapper.ProjectMemberMapper;
import com.lamdayne.humify.project.repository.ProjectInvitationRepository;
import com.lamdayne.humify.project.repository.ProjectMemberRepository;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.repository.ProjectRoleRepository;
import com.lamdayne.humify.project.service.ProjectInvitationService;
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectInvitationServiceImpl implements ProjectInvitationService {

    @Value("${system.url}")
    private String systemUrl; // Sử dụng để sinh `inviteLink` động

    private final EntityManager em;
    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationRepository projectInvitationRepository;

    @Override
    @Transactional
    public InvitationResponse createInvitation(Long projectId, UserPrincipal userPrincipal, CreateInvitationRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        ProjectRole projectRole = projectRoleRepository.findById(request.getProjectRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_ROLE_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        Instant expiredAt = Instant.now().plus(request.getTtlMinutes(), ChronoUnit.MINUTES);

        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .projectRole(projectRole)
                .inviter(em.getReference(User.class, userPrincipal.getId()))
                .email(request.getEmail())
                .token(token)
                .status(ProjectInvitationStatus.PENDING)
                .expiredAt(expiredAt)
                .build();

        projectInvitationRepository.save(invitation);

        String inviteLink = String.format("%s/invite?token=%s", systemUrl, token);

        return InvitationResponse.builder()
                .id(invitation.getId())
                .projectId(project.getId())
                .projectRoleId(projectRole.getId())
                .email(invitation.getEmail())
                .token(invitation.getToken())
                .inviteLink(inviteLink)
                .inviterId(userPrincipal.getId())
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiredAt())
                .createdAt(invitation.getCreatedAt())
                .build();
    }

    @Override
    public ValidateInvitationResponse validateInvitation(String token) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        boolean isValid = true;

        if (invitation.getExpiredAt().isBefore(Instant.now())) {
            isValid = false;
        }
        if (invitation.getStatus() != ProjectInvitationStatus.PENDING) {
            isValid = false;
        }

        return ValidateInvitationResponse.builder()
                .isValid(isValid)
                .projectId(invitation.getProject().getId())
                .projectName(invitation.getProject().getName())
                .roleCode(invitation.getProjectRole().getCode())
                .roleName(invitation.getProjectRole().getName())
                .email(invitation.getEmail())
                .companyCode(invitation.getProject().getCompany().getCompanyCode())
                .build();
    }

    @Override
    @Transactional
    public ProjectMemberResponse acceptInvitation(UserPrincipal userPrincipal, AcceptInvitationRequest request) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        if (invitation.getExpiredAt().isBefore(Instant.now())) {
            invitation.setStatus(ProjectInvitationStatus.EXPIRED);
            projectInvitationRepository.save(invitation);
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        if (invitation.getStatus() == ProjectInvitationStatus.REVOKED) {
            throw new AppException(ErrorCode.INVITATION_REVOKED);
        }

        if (invitation.getStatus() == ProjectInvitationStatus.ACCEPTED) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        projectMemberRepository.findByProjectIdAndUserId(invitation.getProject().getId(), userPrincipal.getId())
                .ifPresent(m -> {
                    throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS);
                });

        ProjectMemberStatus targetStatus = ProjectMemberStatus.ACTIVE;

        if (invitation.getEmail() != null && !invitation.getEmail().equalsIgnoreCase(userPrincipal.getEmail())) {
            targetStatus = ProjectMemberStatus.PENDING_APPROVAL;
        } else {
            invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
            projectInvitationRepository.save(invitation);
        }

        ProjectMember member = ProjectMember.builder()
                .project(invitation.getProject())
                .user(em.getReference(User.class, userPrincipal.getId()))
                .projectRole(invitation.getProjectRole())
                .status(targetStatus)
                .invitedEmail(invitation.getEmail())
                .joinedAt(targetStatus == ProjectMemberStatus.ACTIVE ? Instant.now() : null)
                .build();

        return projectMemberMapper.toProjectMemberResponse(projectMemberRepository.save(member));
    }
}