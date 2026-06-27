package com.lamdayne.humify.task.controller;


import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.task.dto.request.CreateCommentRequest;
import com.lamdayne.humify.task.dto.request.UpdateCommentRequest;
import com.lamdayne.humify.task.dto.response.CommentResponse;
import com.lamdayne.humify.task.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;

    // 1. Tạo bình luận mới (Hoặc trả lời bình luận)
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COMMENT_CREATE_SUCCESS,
                        taskCommentService.createComment(taskId, request, userPrincipal.getId())
                ));
    }

    // 2. Lấy danh sách bình luận của Task
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long taskId) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COMMENT_READ_SUCCESS,
                        taskCommentService.getCommentsByTaskId(taskId)
                ));
    }

    // 3. Sửa nội dung bình luận
    @PutMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.COMMENT_UPDATE_SUCCESS,
                        taskCommentService.updateComment(id, request, userPrincipal.getId())
                ));
    }

    // 4. Xóa bình luận
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        taskCommentService.deleteComment(id, userPrincipal.getId());

        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.COMMENT_DELETE_SUCCESS));
    }
}
