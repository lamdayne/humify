package com.lamdayne.humify.task.service.impl;


import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.task.dto.request.*;
import com.lamdayne.humify.task.dto.response.CommentResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskComment;
import com.lamdayne.humify.task.mapper.TaskCommentMapper;
import com.lamdayne.humify.task.repository.TaskCommentRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.service.TaskCommentService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskCommentMapper taskCommentMapper;

    @Override
    @Transactional
    public CommentResponse createComment(Long taskId, CreateCommentRequest request, Long currentUserId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TaskComment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = taskCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        }

        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .parent(parentComment)
                .content(request.getContent())
                .build();

        return taskCommentMapper.toResponse(taskCommentRepository.save(comment));
    }

    @Override
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new AppException(ErrorCode.TASK_NOT_FOUND);
        }
        return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                .map(taskCommentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long currentUserId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // Kiểm tra quyền: Chỉ tác giả mới được phép sửa bình luận của mình
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }

        comment.setContent(request.getContent());
        return taskCommentMapper.toResponse(taskCommentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // Kiểm tra quyền: Chỉ tác giả mới được phép xóa bình luận
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN_ACTION);
        }

        // Xóa dây chuyền (Cascading Delete): Phải xóa tất cả các "reply" (bình luận con) trước
        taskCommentRepository.deleteByParentId(comment.getId());

        // Cuối cùng mới xóa bình luận gốc (cha)
        taskCommentRepository.delete(comment);
    }
}