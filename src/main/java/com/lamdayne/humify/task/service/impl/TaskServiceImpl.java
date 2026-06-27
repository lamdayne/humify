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
import com.lamdayne.humify.project.service.BoardColumnService;
import com.lamdayne.humify.project.service.ProjectService;
import com.lamdayne.humify.project.service.SprintService;
import com.lamdayne.humify.task.dto.request.AssignTaskRequest;
import com.lamdayne.humify.task.dto.request.CreateTaskRequest;
import com.lamdayne.humify.task.dto.request.MoveTaskRequest;
import com.lamdayne.humify.task.dto.request.UpdateTaskRequest;
import com.lamdayne.humify.task.dto.response.TaskDetailResponse;
import com.lamdayne.humify.task.dto.response.TaskResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.enums.TaskPriority;
import com.lamdayne.humify.task.enums.TaskType;
import com.lamdayne.humify.task.mapper.TaskMapper;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.service.TaskService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        long next = project.getIssueCounter() + 1;
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

        return taskMapper.toResponse(taskRepository.save(task));
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
        return null;
    }

    @Override
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        return null;
    }

    @Override
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {
        return null;
    }

    @Override
    public TaskResponse moveTask(Long taskId, MoveTaskRequest request) {
        return null;
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));
        taskRepository.delete(task);
    }

}
