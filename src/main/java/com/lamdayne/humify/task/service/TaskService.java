package com.lamdayne.humify.task.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.task.dto.request.*;
import com.lamdayne.humify.task.dto.response.TaskDetailResponse;
import com.lamdayne.humify.task.dto.response.TaskResponse;

public interface TaskService {

    TaskResponse createTask(UserPrincipal reporter, Long projectId, CreateTaskRequest request);

    PageResponse<TaskResponse> getTaskByProjectId(Long projectId, int page, int size, String... sorts);

    TaskDetailResponse getTaskById(Long id);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request);

    TaskResponse assignTask(Long taskId, AssignTaskRequest request);

    TaskResponse moveTask(Long taskId, MoveTaskRequest request);

    void deleteTask(Long id);

    TaskResponse reorderTask(Long taskId, ReorderTaskRequest request);

}
