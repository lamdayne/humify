package com.lamdayne.humify.task.service;

import com.lamdayne.humify.task.dto.request.CreateCommentRequest;
import com.lamdayne.humify.task.dto.request.UpdateCommentRequest;
import com.lamdayne.humify.task.dto.response.CommentResponse;

import java.util.List;

public interface TaskCommentService {

    CommentResponse createComment(Long taskId, CreateCommentRequest request, Long currentUserId);
    List<CommentResponse> getCommentsByTaskId(Long taskId);
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long currentUserId);
    void deleteComment(Long commentId, Long currentUserId);
}
