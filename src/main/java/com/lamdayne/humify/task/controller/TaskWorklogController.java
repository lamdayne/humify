package com.lamdayne.humify.task.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.task.dto.request.UpdateWorklogRequest;
import com.lamdayne.humify.task.dto.response.WorklogResponse;
import com.lamdayne.humify.task.service.TaskWorkLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/worklogs")
public class TaskWorklogController {

    private final TaskWorkLogService taskWorkLogService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorklogResponse>> updateWorklog(
            @PathVariable Long id, @RequestBody @Valid UpdateWorklogRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.WORKLOG_UPDATE_SUCCESS,
                        taskWorkLogService.update(id, request))
                );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorklog(
            @PathVariable long id
    ) {
        taskWorkLogService.delete(id);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.WORKLOG_DELETE_SUCCESS));
    }

}
