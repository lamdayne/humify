package com.lamdayne.humify.project.service;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.project.dto.request.CreateSprintRequest;
import com.lamdayne.humify.project.dto.request.UpdateSprintRequest;
import com.lamdayne.humify.project.dto.request.UpdateSprintStatusRequest;
import com.lamdayne.humify.project.dto.response.SprintResponse;
import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.entity.Sprint;
import com.lamdayne.humify.project.enums.SprintStatus;
import com.lamdayne.humify.project.mapper.SprintMapper;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.repository.SprintRepository;
import com.lamdayne.humify.project.service.impl.SprintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SprintServiceImpl Tests")
class SprintServiceImplTest {

    @Mock private SprintRepository sprintRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private SprintMapper sprintMapper;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private Project project;
    private Sprint sprint;
    private SprintResponse sprintResponse;

    @BeforeEach
    void setUp() {
        project = new Project();
        ReflectionTestUtils.setField(project, "id", 1L);

        sprint = Sprint.builder()
                .project(project)
                .name("Sprint 1")
                .goal("Hoan thanh feature A")
                .startDate(Instant.parse("2026-08-01T00:00:00Z"))
                .endDate(Instant.parse("2026-08-14T00:00:00Z"))
                .status(SprintStatus.PLANNED)
                .build();
        ReflectionTestUtils.setField(sprint, "id", 1L);

        sprintResponse = new SprintResponse();
    }

    // ---- createSprint ----

    @Test
    @DisplayName("Create sprint successfully")
    void createSprint_success() {
        CreateSprintRequest request = CreateSprintRequest.builder()
                .name("Sprint 1")
                .goal("Hoan thanh feature A")
                .startDate(Instant.parse("2026-08-01T00:00:00Z"))
                .endDate(Instant.parse("2026-08-14T00:00:00Z"))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse result = sprintService.createSprint(1L, request);

        assertThat(result).isNotNull();
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    @DisplayName("Create sprint throws exception when project not found")
    void createSprint_throwsWhenProjectNotFound() {
        CreateSprintRequest request = CreateSprintRequest.builder()
                .name("Sprint X")
                .build();

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.createSprint(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PROJECT_NOT_FOUND));
    }

    // ---- getSprints ----

    @Test
    @DisplayName("Get sprints by status successfully")
    void getSprints_withStatus() {
        when(sprintRepository.findByProjectIdAndStatus(1L, SprintStatus.PLANNED)).thenReturn(List.of(sprint));
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        List<SprintResponse> result = sprintService.getSprints(1L, SprintStatus.PLANNED);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get all sprints when status is null")
    void getSprints_withNullStatus() {
        when(sprintRepository.findByProjectId(1L)).thenReturn(List.of(sprint));
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        List<SprintResponse> result = sprintService.getSprints(1L, null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get sprints returns empty list")
    void getSprints_empty() {
        when(sprintRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        List<SprintResponse> result = sprintService.getSprints(1L, null);

        assertThat(result).isEmpty();
    }

    // ---- updateSprint ----

    @Test
    @DisplayName("Update sprint successfully")
    void updateSprint_success() {
        UpdateSprintRequest request = UpdateSprintRequest.builder()
                .name("Sprint 1 Updated")
                .goal("Updated goal")
                .startDate(Instant.parse("2026-08-01T00:00:00Z"))
                .endDate(Instant.parse("2026-08-21T00:00:00Z"))
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(any())).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse result = sprintService.updateSprint(1L, request);

        assertThat(result).isNotNull();
        assertThat(sprint.getName()).isEqualTo("Sprint 1 Updated");
    }

    @Test
    @DisplayName("Update sprint throws exception when sprint not found")
    void updateSprint_throwsWhenNotFound() {
        UpdateSprintRequest request = UpdateSprintRequest.builder().name("X").build();

        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.updateSprint(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SPRINT_NOT_FOUND));
    }

    // ---- updateSprintStatus ----

    @Test
    @DisplayName("Update sprint status to ACTIVE successfully")
    void updateSprintStatus_toActive_success() {
        UpdateSprintStatusRequest request = UpdateSprintStatusRequest.builder()
                .status("ACTIVE")
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByProjectIdAndStatus(1L, SprintStatus.ACTIVE)).thenReturn(false);
        when(sprintRepository.save(any())).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse result = sprintService.updateSprintStatus(1L, request);

        assertThat(result).isNotNull();
        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.ACTIVE);
    }

    @Test
    @DisplayName("Update sprint status to ACTIVE throws exception when another sprint is active")
    void updateSprintStatus_toActive_throwsWhenAnotherActive() {
        UpdateSprintStatusRequest request = UpdateSprintStatusRequest.builder()
                .status("ACTIVE")
                .build();

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.existsByProjectIdAndStatus(1L, SprintStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> sprintService.updateSprintStatus(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SPRINT_ALREADY_ACTIVE));
    }

    @Test
    @DisplayName("Update sprint status to COMPLETED successfully")
    void updateSprintStatus_toCompleted_success() {
        UpdateSprintStatusRequest request = UpdateSprintStatusRequest.builder()
                .status("COMPLETED")
                .build();
        sprint.setStatus(SprintStatus.ACTIVE);

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(any())).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        SprintResponse result = sprintService.updateSprintStatus(1L, request);

        assertThat(result).isNotNull();
        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.COMPLETED);
    }

    @Test
    @DisplayName("Update sprint status to ACTIVE successfully when already active")
    void updateSprintStatus_alreadyActive_toActive() {
        UpdateSprintStatusRequest request = UpdateSprintStatusRequest.builder()
                .status("ACTIVE")
                .build();
        sprint.setStatus(SprintStatus.ACTIVE); // Sprint hien tai da la ACTIVE

        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.save(any())).thenReturn(sprint);
        when(sprintMapper.toResponse(sprint)).thenReturn(sprintResponse);

        // Khi sprint da la ACTIVE, khong check existsByProjectIdAndStatus
        SprintResponse result = sprintService.updateSprintStatus(1L, request);

        assertThat(result).isNotNull();
        verify(sprintRepository, never()).existsByProjectIdAndStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Update sprint status throws exception when sprint not found")
    void updateSprintStatus_throwsWhenNotFound() {
        UpdateSprintStatusRequest request = UpdateSprintStatusRequest.builder().status("ACTIVE").build();
        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.updateSprintStatus(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SPRINT_NOT_FOUND));
    }

    // ---- deleteSprint ----

    @Test
    @DisplayName("Delete sprint successfully")
    void deleteSprint_success() {
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        sprintService.deleteSprint(1L);

        verify(sprintRepository).delete(sprint);
    }

    @Test
    @DisplayName("Delete sprint throws exception when sprint not found")
    void deleteSprint_throwsWhenNotFound() {
        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.deleteSprint(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SPRINT_NOT_FOUND));
    }

    // ---- findById ----

    @Test
    @DisplayName("Find sprint by ID successfully")
    void findById_success() {
        when(sprintRepository.findById(1L)).thenReturn(Optional.of(sprint));

        Sprint result = sprintService.findById(1L);

        assertThat(result).isEqualTo(sprint);
    }

    @Test
    @DisplayName("Find sprint by ID throws exception when sprint not found")
    void findById_throwsWhenNotFound() {
        when(sprintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.findById(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.SPRINT_NOT_FOUND));
    }
}