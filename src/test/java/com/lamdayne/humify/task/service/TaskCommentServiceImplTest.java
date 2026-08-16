package com.lamdayne.humify.task.service;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.task.dto.request.CreateCommentRequest;
import com.lamdayne.humify.task.dto.request.UpdateCommentRequest;
import com.lamdayne.humify.task.dto.response.CommentResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskActivity;
import com.lamdayne.humify.task.entity.TaskComment;
import com.lamdayne.humify.task.mapper.TaskCommentMapper;
import com.lamdayne.humify.task.repository.TaskActivityRepository;
import com.lamdayne.humify.task.repository.TaskCommentRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.service.impl.TaskCommentServiceImpl;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskCommentServiceImpl Tests")
class TaskCommentServiceImplTest {

    @Mock private TaskCommentRepository taskCommentRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskCommentMapper taskCommentMapper;
    @Mock private TaskActivityRepository taskActivityRepository;

    @InjectMocks
    private TaskCommentServiceImpl taskCommentService;

    private Task task;
    private User author;
    private TaskComment comment;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        task = new Task();
        ReflectionTestUtils.setField(task, "id", 1L);

        author = new User();
        ReflectionTestUtils.setField(author, "id", 10L);

        comment = TaskComment.builder()
                .task(task)
                .author(author)
                .content("Day la binh luan dau tien")
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);

        commentResponse = new CommentResponse();
    }

    // ---- createComment ----

    @Test
    @DisplayName("Create comment successfully with no parent")
    void createComment_success_noParent() {
        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("Day la binh luan dau tien")
                .parentId(null)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));
        when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(comment);
        when(taskActivityRepository.save(any(TaskActivity.class))).thenReturn(new TaskActivity());
        when(taskCommentMapper.toResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = taskCommentService.createComment(1L, request, 10L);

        assertThat(result).isNotNull();
        verify(taskCommentRepository).save(any(TaskComment.class));
        verify(taskActivityRepository).save(any(TaskActivity.class));
    }

    @Test
    @DisplayName("Create comment reply successfully with parent")
    void createComment_success_withParent() {
        TaskComment parentComment = TaskComment.builder()
                .task(task).author(author).content("Binh luan cha")
                .build();
        ReflectionTestUtils.setField(parentComment, "id", 5L);

        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("Day la reply")
                .parentId(5L)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));
        when(taskCommentRepository.findById(5L)).thenReturn(Optional.of(parentComment));
        when(taskCommentRepository.save(any())).thenReturn(comment);
        when(taskActivityRepository.save(any())).thenReturn(new TaskActivity());
        when(taskCommentMapper.toResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = taskCommentService.createComment(1L, request, 10L);

        assertThat(result).isNotNull();
        verify(taskCommentRepository).findById(5L);
    }

    @Test
    @DisplayName("Create comment throws exception when task not found")
    void createComment_throwsWhenTaskNotFound() {
        CreateCommentRequest request = CreateCommentRequest.builder().content("X").build();
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.createComment(99L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.TASK_NOT_FOUND));
    }

    @Test
    @DisplayName("Create comment throws exception when user not found")
    void createComment_throwsWhenUserNotFound() {
        CreateCommentRequest request = CreateCommentRequest.builder().content("X").build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.createComment(1L, request, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("Create comment throws exception when parent comment not found")
    void createComment_throwsWhenParentNotFound() {
        CreateCommentRequest request = CreateCommentRequest.builder()
                .content("Reply")
                .parentId(999L)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(10L)).thenReturn(Optional.of(author));
        when(taskCommentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.createComment(1L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
    }

    // ---- getCommentsByTaskId ----

    @Test
    @DisplayName("Get comments by task ID returns valid list")
    void getCommentsByTaskId_success() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(comment));
        when(taskCommentMapper.toResponse(comment)).thenReturn(commentResponse);

        List<CommentResponse> result = taskCommentService.getCommentsByTaskId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get comments by task ID returns empty list")
    void getCommentsByTaskId_empty() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(Collections.emptyList());

        List<CommentResponse> result = taskCommentService.getCommentsByTaskId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Get comments by task ID throws exception when task not found")
    void getCommentsByTaskId_throwsWhenTaskNotFound() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskCommentService.getCommentsByTaskId(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.TASK_NOT_FOUND));
    }

    // ---- updateComment ----

    @Test
    @DisplayName("Update comment successfully when user is the author")
    void updateComment_success() {
        UpdateCommentRequest request = UpdateCommentRequest.builder()
                .content("Noi dung cap nhat")
                .build();

        when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(taskCommentRepository.save(any())).thenReturn(comment);
        when(taskCommentMapper.toResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = taskCommentService.updateComment(1L, request, 10L);

        assertThat(result).isNotNull();
        assertThat(comment.getContent()).isEqualTo("Noi dung cap nhat");
    }

    @Test
    @DisplayName("Update comment throws exception when comment not found")
    void updateComment_throwsWhenNotFound() {
        UpdateCommentRequest request = UpdateCommentRequest.builder().content("X").build();
        when(taskCommentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.updateComment(99L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("Update comment throws exception when user is not the author")
    void updateComment_throwsWhenNotAuthor() {
        UpdateCommentRequest request = UpdateCommentRequest.builder().content("X").build();

        when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> taskCommentService.updateComment(1L, request, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN_ACTION));
    }

    // ---- deleteComment ----

    @Test
    @DisplayName("Delete comment successfully with child replies")
    void deleteComment_success() {
        when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        taskCommentService.deleteComment(1L, 10L);

        verify(taskCommentRepository).deleteByParentId(1L);
        verify(taskCommentRepository).delete(comment);
    }

    @Test
    @DisplayName("Delete comment throws exception when comment not found")
    void deleteComment_throwsWhenNotFound() {
        when(taskCommentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskCommentService.deleteComment(99L, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Test
    @DisplayName("Delete comment throws exception when user is not the author")
    void deleteComment_throwsWhenNotAuthor() {
        when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> taskCommentService.deleteComment(1L, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN_ACTION));

        verify(taskCommentRepository, never()).delete(any());
    }
}