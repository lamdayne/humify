package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.mail.dto.SendEmailEvent;
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
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectInvitationServiceImpl Unit Tests")
class ProjectInvitationServiceImplTest {

    @Mock private EntityManager em;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectRoleRepository projectRoleRepository;
    @Mock private ProjectMemberMapper projectMemberMapper;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private ProjectInvitationRepository projectInvitationRepository;

    @InjectMocks
    private ProjectInvitationServiceImpl invitationService;

    private Company company;
    private Project project;
    private ProjectRole projectRole;
    private User inviter;
    private UserPrincipal userPrincipal;
    private ProjectInvitation invitation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invitationService, "systemUrl", "https://humify.app");

        company = Company.builder().build();
        company.setId(1L);
        company.setCompanyCode("HUMIFY");

        project = Project.builder().company(company).name("Humify Core").build();
        project.setId(10L);

        projectRole = new ProjectRole();
        projectRole.setId(20L);
        projectRole.setCode("MEMBER");
        projectRole.setName("Member");

        inviter = User.builder().email("inviter@humify.com").build();
        inviter.setId(100L);

        userPrincipal = UserPrincipal.builder().id(100L).email("inviter@humify.com").build();

        invitation = ProjectInvitation.builder()
                .project(project)
                .projectRole(projectRole)
                .inviter(inviter)
                .email("invitee@humify.com")
                .token("abc-token")
                .status(ProjectInvitationStatus.PENDING)
                .expiredAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        invitation.setId(30L);
    }

    // ---- createInvitation ----

    @Test
    @DisplayName("createInvitation - success, sends email when email is provided")
    void createInvitation_success_sendsEmail() {
        CreateInvitationRequest request = mock(CreateInvitationRequest.class);
        when(request.getProjectRoleId()).thenReturn(20L);
        when(request.getTtlMinutes()).thenReturn(10080L);
        when(request.getEmail()).thenReturn("invitee@humify.com");

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(20L)).thenReturn(Optional.of(projectRole));
        when(em.getReference(User.class, 100L)).thenReturn(inviter);
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        InvitationResponse result = invitationService.createInvitation(10L, userPrincipal, request);

        assertNotNull(result);
        assertThat(result.getProjectId()).isEqualTo(10L);
        assertThat(result.getInviteLink()).isEqualTo("https://humify.app/invite?token=" + result.getToken());
        verify(applicationEventPublisher).publishEvent(any(SendEmailEvent.class));
    }

    @Test
    @DisplayName("createInvitation - success, no email, does not send email")
    void createInvitation_success_noEmail_noEventPublished() {
        CreateInvitationRequest request = mock(CreateInvitationRequest.class);
        when(request.getProjectRoleId()).thenReturn(20L);
        when(request.getTtlMinutes()).thenReturn(10080L);
        when(request.getEmail()).thenReturn(null);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(20L)).thenReturn(Optional.of(projectRole));
        when(em.getReference(User.class, 100L)).thenReturn(inviter);
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        InvitationResponse result = invitationService.createInvitation(10L, userPrincipal, request);

        assertNotNull(result);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    @DisplayName("createInvitation - fail when project not found")
    void createInvitation_projectNotFound_throwsException() {
        CreateInvitationRequest request = mock(CreateInvitationRequest.class);
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.createInvitation(999L, userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    @DisplayName("createInvitation - fail when project role not found")
    void createInvitation_roleNotFound_throwsException() {
        CreateInvitationRequest request = mock(CreateInvitationRequest.class);
        when(request.getProjectRoleId()).thenReturn(999L);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRoleRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.createInvitation(10L, userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROJECT_ROLE_NOT_FOUND);
    }

    // ---- validateInvitation ----

    @Test
    @DisplayName("validateInvitation - success, valid invitation")
    void validateInvitation_valid() {
        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));

        ValidateInvitationResponse result = invitationService.validateInvitation("abc-token");

        assertTrue(result.isValid());
        assertThat(result.getProjectId()).isEqualTo(10L);
        assertThat(result.getCompanyCode()).isEqualTo("HUMIFY");
    }

    @Test
    @DisplayName("validateInvitation - invalid when expired")
    void validateInvitation_expired_invalid() {
        invitation.setExpiredAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));

        ValidateInvitationResponse result = invitationService.validateInvitation("abc-token");

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("validateInvitation - invalid when status is not PENDING")
    void validateInvitation_notPending_invalid() {
        invitation.setStatus(ProjectInvitationStatus.ACCEPTED);
        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));

        ValidateInvitationResponse result = invitationService.validateInvitation("abc-token");

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("validateInvitation - fail when token not found")
    void validateInvitation_notFound_throwsException() {
        when(projectInvitationRepository.findByToken("invalid")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.validateInvitation("invalid"));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVITATION_NOT_FOUND);
    }

    // ---- acceptInvitation ----

    @Test
    @DisplayName("acceptInvitation - success, email matches, becomes ACTIVE")
    void acceptInvitation_success_emailMatches_active() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("abc-token");

        invitation.setEmail("inviter@humify.com"); // matches userPrincipal email

        ProjectMember savedMember = ProjectMember.builder().project(project).status(ProjectMemberStatus.ACTIVE).build();
        savedMember.setId(50L);
        ProjectMemberResponse response = mock(ProjectMemberResponse.class);

        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 100L)).thenReturn(Optional.empty());
        when(em.getReference(User.class, 100L)).thenReturn(inviter);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(savedMember);
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(response);

        ProjectMemberResponse result = invitationService.acceptInvitation(userPrincipal, request);

        assertNotNull(result);
        assertThat(invitation.getStatus()).isEqualTo(ProjectInvitationStatus.ACCEPTED);
        verify(projectInvitationRepository).save(invitation);
    }

    @Test
    @DisplayName("acceptInvitation - success, email mismatch, becomes PENDING_APPROVAL")
    void acceptInvitation_success_emailMismatch_pendingApproval() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("abc-token");

        invitation.setEmail("someone-else@humify.com");

        ProjectMember savedMember = ProjectMember.builder().project(project).status(ProjectMemberStatus.PENDING_APPROVAL).build();
        savedMember.setId(50L);
        ProjectMemberResponse response = mock(ProjectMemberResponse.class);

        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 100L)).thenReturn(Optional.empty());
        when(em.getReference(User.class, 100L)).thenReturn(inviter);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(savedMember);
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(response);

        ProjectMemberResponse result = invitationService.acceptInvitation(userPrincipal, request);

        assertNotNull(result);
        // invitation status untouched (still PENDING) since email mismatch branch doesn't update it
        assertThat(invitation.getStatus()).isEqualTo(ProjectInvitationStatus.PENDING);
        verify(projectInvitationRepository, never()).save(invitation);
    }

    @Test
    @DisplayName("acceptInvitation - fail when token not found")
    void acceptInvitation_notFound_throwsException() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("invalid");
        when(projectInvitationRepository.findByToken("invalid")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.acceptInvitation(userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVITATION_NOT_FOUND);
    }

    @Test
    @DisplayName("acceptInvitation - fail when expired, sets status EXPIRED")
    void acceptInvitation_expired_throwsException() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("abc-token");
        invitation.setExpiredAt(Instant.now().minus(1, ChronoUnit.DAYS));

        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.acceptInvitation(userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVITATION_EXPIRED);
        assertThat(invitation.getStatus()).isEqualTo(ProjectInvitationStatus.EXPIRED);
        verify(projectInvitationRepository).save(invitation);
    }

    @Test
    @DisplayName("acceptInvitation - fail when invitation revoked")
    void acceptInvitation_revoked_throwsException() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("abc-token");
        invitation.setStatus(ProjectInvitationStatus.REVOKED);

        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.acceptInvitation(userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVITATION_REVOKED);
    }

    @Test
    @DisplayName("acceptInvitation - fail when already accepted")
    void acceptInvitation_alreadyAccepted_throwsException() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("abc-token");
        invitation.setStatus(ProjectInvitationStatus.ACCEPTED);

        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.acceptInvitation(userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("acceptInvitation - fail when user is already a project member")
    void acceptInvitation_alreadyMember_throwsException() {
        AcceptInvitationRequest request = mock(AcceptInvitationRequest.class);
        when(request.getToken()).thenReturn("abc-token");

        ProjectMember existingMember = ProjectMember.builder().build();
        existingMember.setId(60L);

        when(projectInvitationRepository.findByToken("abc-token")).thenReturn(Optional.of(invitation));
        when(projectMemberRepository.findByProjectIdAndUserId(10L, 100L)).thenReturn(Optional.of(existingMember));

        AppException exception = assertThrows(AppException.class,
                () -> invitationService.acceptInvitation(userPrincipal, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_ALREADY_EXISTS);
        verify(projectMemberRepository, never()).save(any());
    }
}