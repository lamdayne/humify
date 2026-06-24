package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.auth.repository.UserHasRoleRepository;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.project.dto.request.CreateInvitationRequest;
import com.lamdayne.humify.project.dto.request.AcceptInvitationRequest;
import com.lamdayne.humify.project.dto.response.InvitationResponse;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.dto.response.ValidateInvitationResponse;
import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.entity.ProjectInvitation;
import com.lamdayne.humify.project.entity.ProjectMember;
import com.lamdayne.humify.project.entity.ProjectRole;
import com.lamdayne.humify.project.enums.ProjectInvitationStatus;
import com.lamdayne.humify.project.enums.ProjectMemberStatus;
import com.lamdayne.humify.project.mapper.ProjectInvitationMapper;
import com.lamdayne.humify.project.mapper.ProjectMemberMapper;
import com.lamdayne.humify.project.repository.ProjectInvitationRepository;
import com.lamdayne.humify.project.repository.ProjectMemberRepository;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.repository.ProjectRoleRepository;
import com.lamdayne.humify.project.service.ProjectInvitationService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectInvitationServiceImpl implements ProjectInvitationService {

    @Value("${system.url}")
    private String systemUrl;

    private final ProjectRepository projectRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final ProjectInvitationMapper projectInvitationMapper;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    @Transactional
    public InvitationResponse createInvitation(Long projectId, CreateInvitationRequest request) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = principal.getId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        List<String> roleNames = userHasRoleRepository.findAllRoleNameByUserId(userId);
        boolean isCompanyAdmin = roleNames.contains("COMPANY_ADMIN") || roleNames.contains("SYSTEM_ADMIN");

        if (!isCompanyAdmin) {
            boolean isOwner = project.getCreator() != null && project.getCreator().getId().equals(userId);
            if (!isOwner) {
                boolean isManager = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                        .map(m -> m.getStatus() == ProjectMemberStatus.ACTIVE
                                && "MANAGER".equals(m.getProjectRole().getCode()))
                        .orElse(false);
                if (!isManager) {
                    throw new AppException(ErrorCode.FORBIDDEN);
                }
            }
        }

        ProjectRole role = projectRoleRepository.findById(request.getProjectRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_ROLE_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                boolean isAlreadyMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userOpt.get().getId())
                        .map(m -> m.getStatus() == ProjectMemberStatus.ACTIVE)
                        .orElse(false);
                if (isAlreadyMember) {
                    throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS);
                }
            }
        }

        String token = UUID.randomUUID().toString();
        Instant expiredAt = Instant.now().plus(request.getTtlMinutes(), ChronoUnit.MINUTES);
        User inviter = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .projectRole(role)
                .inviter(inviter)
                .email(request.getEmail())
                .token(token)
                .status(ProjectInvitationStatus.PENDING)
                .expiredAt(expiredAt)
                .build();

        invitation = projectInvitationRepository.save(invitation);

        InvitationResponse response = projectInvitationMapper.toInvitationResponse(invitation);
        response.setInviteLink(systemUrl + "/invite?token=" + token);
        return response;
    }

    @Override
    @Transactional
    public ValidateInvitationResponse validateInvitation(String token) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        if (invitation.getStatus() == ProjectInvitationStatus.REVOKED) {
            throw new AppException(ErrorCode.INVITATION_REVOKED);
        }
        if (invitation.getStatus() == ProjectInvitationStatus.ACCEPTED) {
            throw new AppException(ErrorCode.INVITATION_NOT_FOUND);
        }
        if (invitation.getStatus() == ProjectInvitationStatus.EXPIRED || Instant.now().isAfter(invitation.getExpiredAt())) {
            if (invitation.getStatus() == ProjectInvitationStatus.PENDING) {
                invitation.setStatus(ProjectInvitationStatus.EXPIRED);
                projectInvitationRepository.save(invitation);
            }
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        return projectInvitationMapper.toValidateInvitationResponse(invitation);
    }

    @Override
    @Transactional
    public ProjectMemberResponse acceptInvitation(UserPrincipal userPrincipal, AcceptInvitationRequest request) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVITATION_NOT_FOUND));

        if (invitation.getStatus() == ProjectInvitationStatus.REVOKED) throw new AppException(ErrorCode.INVITATION_REVOKED);
        if (invitation.getStatus() == ProjectInvitationStatus.ACCEPTED) throw new AppException(ErrorCode.INVITATION_NOT_FOUND);
        if (invitation.getStatus() == ProjectInvitationStatus.EXPIRED || Instant.now().isAfter(invitation.getExpiredAt())) {
            if (invitation.getStatus() == ProjectInvitationStatus.PENDING) {
                invitation.setStatus(ProjectInvitationStatus.EXPIRED);
                projectInvitationRepository.save(invitation);
            }
            throw new AppException(ErrorCode.INVITATION_EXPIRED);
        }

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ProjectMemberStatus memberStatus = ProjectMemberStatus.ACTIVE;
        if (invitation.getEmail() != null && !invitation.getEmail().isBlank()) {
            if (!invitation.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
                memberStatus = ProjectMemberStatus.PENDING_APPROVAL;
            }
        }

        Optional<ProjectMember> existingMemberOpt = projectMemberRepository
                .findByProjectIdAndUserId(invitation.getProject().getId(), currentUser.getId());

        ProjectMember member;
        if (existingMemberOpt.isPresent()) {
            member = existingMemberOpt.get();
            if (member.getStatus() == ProjectMemberStatus.ACTIVE) {
                throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS);
            }
            member.setProjectRole(invitation.getProjectRole());
            member.setStatus(memberStatus);
            member.setJoinedAt(Instant.now());
        } else {
            member = ProjectMember.builder()
                    .project(invitation.getProject())
                    .user(currentUser)
                    .projectRole(invitation.getProjectRole())
                    .status(memberStatus)
                    .invitedEmail(invitation.getEmail())
                    .joinedAt(Instant.now())
                    .build();
        }

        member = projectMemberRepository.save(member);

        if (memberStatus == ProjectMemberStatus.ACTIVE) {
            invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
            projectInvitationRepository.save(invitation);
        }

        return projectMemberMapper.toProjectMemberResponse(member);
    }
}