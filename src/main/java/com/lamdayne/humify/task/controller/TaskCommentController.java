package com.lamdayne.humify.task.controller;


import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.task.dto.request.UpdateCommentRequest;
import com.lamdayne.humify.task.dto.response.CommentResponse;
import com.lamdayne.humify.task.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;


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

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        taskCommentService.deleteComment(id, userPrincipal.getId());
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.COMMENT_DELETE_SUCCESS));
    }
}
