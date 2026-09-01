package com.lamdayne.humify.task.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.task.dto.request.*;
import com.lamdayne.humify.task.dto.response.TaskDetailResponse;
import com.lamdayne.humify.task.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(UserPrincipal reporter, Long projectId, CreateTaskRequest request);

    PageResponse<TaskResponse> getTaskByProjectId(Long projectId, int page, int size, String... sorts);

    TaskDetailResponse getTaskById(Long id);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request);

    TaskResponse assignTask(Long taskId, AssignTaskRequest request);

    TaskResponse moveTask(Long taskId, MoveTaskRequest request);

    void deleteTask(Long id);

    TaskResponse reorderTask(Long taskId, ReorderTaskRequest request);

    long countEligibleTasks(
            Long userId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    long countCompletedEligibleTasks(
            Long userId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    long countOnTimeEligibleTasks(
            Long userId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    List<TaskResponse> filterTasksByProjectId(Long projectId, Pageable pageable, String[] params);

}
