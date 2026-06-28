package com.lamdayne.humify.task.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.task.dto.request.*;
import com.lamdayne.humify.task.dto.response.AttachmentResponse;
import com.lamdayne.humify.task.dto.response.TaskDetailResponse;
import com.lamdayne.humify.task.dto.response.TaskResponse;
import com.lamdayne.humify.task.dto.response.WorklogResponse;
import com.lamdayne.humify.task.service.TaskAttachmentService;
import com.lamdayne.humify.task.service.TaskService;
import com.lamdayne.humify.task.service.TaskWorkLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskWorkLogService taskWorkLogService;
    private final TaskAttachmentService taskAttachmentService;

    @PostMapping(value = "/{taskId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file
    ) {
        AttachmentResponse response = taskAttachmentService.addAttachment(userPrincipal, taskId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.ATTACHMENT_UPLOAD_SUCCESS, response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDetailResponse>> getTaskById(
            @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.TASK_READ_SUCCESS,
                        taskService.getTaskById(id)
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateTaskRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.TASK_UPDATE_SUCCESS,
                        taskService.updateTask(id, request)
                ));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid AssignTaskRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.TASK_ASSIGN_SUCCESS,
                        taskService.assignTask(id, request)
                ));
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<ApiResponse<TaskResponse>> moveTask(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid MoveTaskRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.TASK_MOVE_SUCCESS,
                        taskService.moveTask(id, request)
                ));
    }

    @PutMapping("/{id}/reorder")
    public ResponseEntity<ApiResponse<TaskResponse>> reorderTask(
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid ReorderTaskRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.TASK_REORDER_SUCCESS,
                        taskService.reorderTask(id, request)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable(name = "id") Long id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.TASK_DELETE_SUCCESS));
    }

    @PostMapping("{taskId}/worklogs")
    public ResponseEntity<ApiResponse<WorklogResponse>> create(@PathVariable Long taskId,
                                                               @RequestBody @Valid CreateWorklogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(SuccessCode.WORKLOG_CREATE_SUCCESS,
                        taskWorkLogService.create(taskId, request)));

    }

    @GetMapping("/{taskId}/worklogs")
    public ResponseEntity<ApiResponse<List<WorklogResponse>>> getAll(
            @PathVariable Long taskId) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORKLOG_READ_SUCCESS,
                        taskWorkLogService.getByTask(taskId)));
    }

}
