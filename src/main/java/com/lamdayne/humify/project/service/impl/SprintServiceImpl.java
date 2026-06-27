package com.lamdayne.humify.project.service.impl;


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
import com.lamdayne.humify.project.service.SprintService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;

    @Override
    @Transactional
    public SprintResponse createSprint(Long projectId, CreateSprintRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Sprint sprint = Sprint.builder()
                .project(project)
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.PLANNED)
                .build();

        return sprintMapper.toResponse(sprintRepository.save(sprint));
    }

    @Override
    public List<SprintResponse> getSprints(Long projectId, SprintStatus status) {
        List<Sprint> sprints;
        if (status != null) {
            sprints = sprintRepository.findByProjectIdAndStatus(projectId, status);
        } else {
            sprints = sprintRepository.findByProjectId(projectId);
        }

        // Dùng mapper ở đây
        return sprints.stream()
                .map(sprintMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SprintResponse updateSprint(Long id, UpdateSprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SPRINT_NOT_FOUND));

        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());

        return sprintMapper.toResponse(sprintRepository.save(sprint));
    }

    @Override
    @Transactional
    public SprintResponse updateSprintStatus(Long id, UpdateSprintStatusRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SPRINT_NOT_FOUND));

        SprintStatus newStatus = SprintStatus.valueOf(request.getStatus());

        if (newStatus == SprintStatus.ACTIVE && sprint.getStatus() != SprintStatus.ACTIVE) {
            boolean hasActiveSprint = sprintRepository.existsByProjectIdAndStatus(
                    sprint.getProject().getId(), SprintStatus.ACTIVE);

            if (hasActiveSprint) {
                throw new AppException(ErrorCode.SPRINT_ALREADY_ACTIVE);
            }
        }

        sprint.setStatus(newStatus);
        return sprintMapper.toResponse(sprintRepository.save(sprint));
    }

    @Override
    @Transactional
    public void deleteSprint(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SPRINT_NOT_FOUND));

        sprintRepository.delete(sprint);

    }

    @Override
    public Sprint findById(Long id) {
        return sprintRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.SPRINT_NOT_FOUND));
    }
}
