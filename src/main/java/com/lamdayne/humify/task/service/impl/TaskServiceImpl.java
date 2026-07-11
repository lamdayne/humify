package com.lamdayne.humify.task.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.project.entity.BoardColumn;
import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.entity.Sprint;
import com.lamdayne.humify.project.enums.ColumnCategory;
import com.lamdayne.humify.project.service.BoardColumnService;
import com.lamdayne.humify.project.service.ProjectService;
import com.lamdayne.humify.project.service.SprintService;
import com.lamdayne.humify.task.dto.request.*;
import com.lamdayne.humify.task.dto.response.SubtaskResponse;
import com.lamdayne.humify.task.dto.response.TaskDetailResponse;
import com.lamdayne.humify.task.dto.response.TaskResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskActivity;
import com.lamdayne.humify.task.enums.TaskActivityAction;
import com.lamdayne.humify.task.enums.TaskPriority;
import com.lamdayne.humify.task.enums.TaskType;
import com.lamdayne.humify.task.mapper.TaskMapper;
import com.lamdayne.humify.task.repository.TaskActivityRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.service.TaskService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final UserService userService;
    private final SprintService sprintService;
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final BoardColumnService boardColumnService;
    private final CompanyAccessService companyAccessService;
    private final TaskActivityRepository taskActivityRepository;

    @Override
    @Transactional
    public TaskResponse createTask(UserPrincipal userPrincipal, Long projectId, CreateTaskRequest request) {
        Project project = projectService.findById(projectId);

        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintService.findById(request.getSprintId());
        }

        BoardColumn boardColumn = boardColumnService.findById(request.getColumnId());
        Long companyId = CompanyContext.getCompanyId();
        Company company = companyAccessService.getReferenceById(companyId);

        Task parent = null;
        if (request.getParentId() != null) {
            parent = taskRepository.findById(request.getParentId()).orElse(null);
        }

        User reporter = userService.getUserById(userPrincipal.getId());
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userService.getUserById(request.getAssigneeId());
        }

        long next = projectService.incrementAndGetIssueCounter(projectId);
        Double maxPosition = taskRepository.findMaxPositionByColumnId(request.getColumnId());

        double newPosition = (maxPosition == null) ? 1000.0 : maxPosition + 1000.0;

        Task task = Task.builder()
                .company(company)
                .project(project)
                .sprint(sprint)
                .column(boardColumn)
                .parent(parent)
                .reporter(reporter)
                .assignee(assignee)
                .taskKey(String.format("%s-%d", project.getKey(), next))
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(TaskPriority.valueOf(request.getPriority()))
                .type(TaskType.valueOf(request.getType()))
                .points(request.getPoints())
                .estimatedHours(request.getEstimatedHours())
                .position(newPosition)
                .dueDate(request.getDueDate())
                .build();

        task = taskRepository.save(task);

        taskActivityRepository.save(TaskActivity.builder()
                .task(task)
                .user(reporter)
                .action(TaskActivityAction.CREATE_TASK)
                .build()
        );

        return taskMapper.toResponse(task);
    }

    @Override
    public PageResponse<TaskResponse> getTaskByProjectId(Long projectId, int page, int size, String... sorts) {
        projectService.findById(projectId);

        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Task> taskPage = taskRepository.findAllByProjectId(projectId, pageable);

        List<TaskResponse> taskResponses = taskPage.stream()
                .map(taskMapper::toResponse).toList();

        return PageResponse.<TaskResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .items(taskResponses)
                .build();
    }

    @Override
    public TaskDetailResponse getTaskById(Long id) {
        Task task = taskRepository.findDetailById(id).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        List<SubtaskResponse> subtasks = taskRepository.findByParentId(id).stream()
                .map(taskMapper::toSubtaskResponse)
                .toList();

        return taskMapper.toDetailResponse(task)
                .toBuilder()
                .subtasks(subtasks)
                .build();
    }

    @Override
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        User user = getUser();

        Task task = taskRepository.findById(taskId).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (!task.getPoints().equals(request.getPoints())) {
            taskActivityRepository.save(TaskActivity.builder()
                    .task(task)
                    .user(user)
                    .action(TaskActivityAction.UPDATE_POINTS)
                    .oldValue(task.getPoints().toString())
                    .newValue(request.getPoints().toString())
                    .build()
            );
        }

        if (task.getPriority() != TaskPriority.valueOf(request.getPriority())) {
            taskActivityRepository.save(TaskActivity.builder()
                    .task(task)
                    .user(user)
                    .action(TaskActivityAction.UPDATE_PRIORITY)
                    .oldValue(task.getPriority().toString())
                    .newValue(request.getPriority())
                    .build()
            );
        }

        taskMapper.updateTask(task, request);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User assignee = userService.getUserById(request.getAssigneeId());
        task.setAssignee(assignee);

        taskActivityRepository.save(TaskActivity.builder()
                .task(task)
                .user(getUser())
                .action(TaskActivityAction.CHANGE_ASSIGNEE)
                .build()
        );

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse moveTask(Long taskId, MoveTaskRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        if (request.getSprintId() != null) {
            Sprint sprint = sprintService.findById(request.getSprintId());
            task.setSprint(sprint);
        }

        BoardColumn boardColumn = new BoardColumn();
        if (request.getColumnId() != null) {
            boardColumn = boardColumnService.findById(request.getColumnId());
            task.setColumn(boardColumn);
        }

        applyColumnTransitionRules(task, boardColumn);

        Double beforePos = resolvePosition(request.getBeforeTaskId());
        Double afterPos = resolvePosition(request.getAfterTaskId());

        task.setPosition(calculatePosition(beforePos, afterPos));

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse reorderTask(Long taskId, ReorderTaskRequest request) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        Double beforePos = resolvePosition(request.getBeforeTaskId());
        Double afterPos = resolvePosition(request.getAfterTaskId());

        task.setPosition(calculatePosition(beforePos, afterPos));

        return taskMapper.toResponse(taskRepository.save(task));
    }

    private void applyColumnTransitionRules(Task task, BoardColumn column) {
        if (column.getCategory().equals(ColumnCategory.DONE) && task.getCompletedAt() == null) {
            task.setCompletedAt(Instant.now());
        }

        if (!column.getCategory().equals(ColumnCategory.DONE) && task.getCompletedAt() != null) {
            task.setCompletedAt(null);
        }
    }

    private Double resolvePosition(Long taskId) {
        if (taskId == null) return null;
        return taskRepository.findById(taskId)
                .map(Task::getPosition)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
    }

    private double calculatePosition(Double beforePos, Double afterPos) {
        final double GAP = 1000.0;
        if (beforePos == null && afterPos == null) return GAP;
        if (beforePos == null) return afterPos / 2.0;
        if (afterPos == null) return beforePos + GAP;
        return (afterPos + beforePos) / 2.0;
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userService.getUserById(userPrincipal.getId());
    }

}
