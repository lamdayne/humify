package com.lamdayne.humify.task.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.task.service.TaskAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attachments")
public class TaskAttachmentController {

    private final TaskAttachmentService taskAttachmentService;

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long id) {
        taskAttachmentService.deleteAttachment(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.ATTACHMENT_DELETE_SUCCESS, null));
    }
}