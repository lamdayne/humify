package com.lamdayne.humify.task.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.task.dto.request.CreateWorklogRequest;
import com.lamdayne.humify.task.dto.request.UpdateWorklogRequest;
import com.lamdayne.humify.task.dto.response.WorklogResponse;
import com.lamdayne.humify.task.service.TaskWorkLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskWorklogController {
    private final TaskWorkLogService taskWorkLogService;

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

    @PutMapping("/worklogs/{id}")
    public ResponseEntity<ApiResponse<WorklogResponse>> updateWorklog(
            @PathVariable Long id, @RequestBody @Valid UpdateWorklogRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORKLOG_UPDATE_SUCCESS,
                        taskWorkLogService.update(id, request)));
    }

    @DeleteMapping("/worklogs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorklog(
            @PathVariable long id

    ) {
        taskWorkLogService.delete(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.WORKLOG_DELETE_SUCCESS));
    }

}
