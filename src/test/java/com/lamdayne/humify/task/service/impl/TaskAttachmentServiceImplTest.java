package com.lamdayne.humify.task.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.media.dto.response.UploadResponse;
import com.lamdayne.humify.media.service.MediaService;
import com.lamdayne.humify.task.dto.request.AddAttachmentRequest;
import com.lamdayne.humify.task.dto.response.AttachmentResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskAttachment;
import com.lamdayne.humify.task.mapper.TaskMapper;
import com.lamdayne.humify.task.repository.TaskActivityRepository;
import com.lamdayne.humify.task.repository.TaskAttachmentRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskAttachmentServiceImpl Unit Tests")
class TaskAttachmentServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskAttachmentRepository taskAttachmentRepository;
    @Mock private UserService userService;
    @Mock private MediaService mediaService;
    @Mock private TaskMapper taskMapper;
    @Mock private TaskActivityRepository taskActivityRepository;

    @InjectMocks
    private TaskAttachmentServiceImpl taskAttachmentService;

    private Task task;
    private User uploader;
    private UserPrincipal userPrincipal;
    private TaskAttachment attachment;

    @BeforeEach
    void setUp() {
        task = Task.builder().title("Fix login bug").build();
        task.setId(1L);

        uploader = User.builder().email("dev@humify.com").build();
        uploader.setId(10L);

        userPrincipal = UserPrincipal.builder().id(10L).email("dev@humify.com").build();

        attachment = TaskAttachment.builder()
                .id(5L)
                .task(task)
                .uploadedBy(uploader)
                .fileName("screenshot.png")
                .fileUrl("https://cdn.humify.com/screenshot.png")
                .fileSize(2048L)
                .build();
    }

    // ---- addAttachment (MultipartFile) ----

    @Test
    @DisplayName("addAttachment(file) - success")
    void addAttachment_withFile_success() {
        MultipartFile file = new MockMultipartFile("file", "screenshot.png", "image/png", "data".getBytes());
        UploadResponse uploadResponse = UploadResponse.builder().url("https://cdn.humify.com/screenshot.png").build();
        AttachmentResponse response = mock(AttachmentResponse.class);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(mediaService.upload(file, "tasks")).thenReturn(uploadResponse);
        when(userService.getUserById(10L)).thenReturn(uploader);
        when(taskAttachmentRepository.save(any(TaskAttachment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toAttachmentResponse(any(TaskAttachment.class))).thenReturn(response);

        AttachmentResponse result = taskAttachmentService.addAttachment(userPrincipal, 1L, file);

        assertNotNull(result);
        verify(taskActivityRepository).save(any());
        verify(taskAttachmentRepository).save(any(TaskAttachment.class));
    }

    @Test
    @DisplayName("addAttachment(file) - fail when task not found")
    void addAttachment_withFile_taskNotFound_throwsException() {
        MultipartFile file = new MockMultipartFile("file", "screenshot.png", "image/png", "data".getBytes());
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> taskAttachmentService.addAttachment(userPrincipal, 999L, file));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_NOT_FOUND);
        verifyNoInteractions(mediaService);
        verify(taskAttachmentRepository, never()).save(any());
    }

    // ---- addAttachment (AddAttachmentRequest) ----

    @Test
    @DisplayName("addAttachment(request) - success")
    void addAttachment_withRequest_success() {
        AddAttachmentRequest request = mock(AddAttachmentRequest.class);
        when(request.getFileName()).thenReturn("design.pdf");
        when(request.getFileUrl()).thenReturn("https://cdn.humify.com/design.pdf");
        when(request.getFileSize()).thenReturn(4096L);

        AttachmentResponse response = mock(AttachmentResponse.class);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getUserById(10L)).thenReturn(uploader);
        when(taskAttachmentRepository.save(any(TaskAttachment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toAttachmentResponse(any(TaskAttachment.class))).thenReturn(response);

        AttachmentResponse result = taskAttachmentService.addAttachment(userPrincipal, 1L, request);

        assertNotNull(result);
        verify(taskAttachmentRepository).save(any(TaskAttachment.class));
        verifyNoInteractions(mediaService);
        verifyNoInteractions(taskActivityRepository);
    }

    @Test
    @DisplayName("addAttachment(request) - fail when task not found")
    void addAttachment_withRequest_taskNotFound_throwsException() {
        AddAttachmentRequest request = mock(AddAttachmentRequest.class);
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> taskAttachmentService.addAttachment(userPrincipal, 999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TASK_NOT_FOUND);
        verify(taskAttachmentRepository, never()).save(any());
    }

    // ---- deleteAttachment ----

    @Test
    @DisplayName("deleteAttachment - success")
    void deleteAttachment_success() {
        when(taskAttachmentRepository.findById(5L)).thenReturn(Optional.of(attachment));

        taskAttachmentService.deleteAttachment(5L);

        verify(taskAttachmentRepository).delete(attachment);
    }

    @Test
    @DisplayName("deleteAttachment - fail when not found")
    void deleteAttachment_notFound_throwsException() {
        when(taskAttachmentRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> taskAttachmentService.deleteAttachment(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(taskAttachmentRepository, never()).delete(any());
    }

    // ---- getAttachments ----

    @Test
    @DisplayName("getAttachments - success")
    void getAttachments_success() {
        when(taskAttachmentRepository.findByTaskId(1L)).thenReturn(List.of(attachment));

        List<AttachmentResponse> result = taskAttachmentService.getAttachments(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("screenshot.png");
        assertThat(result.get(0).getTaskId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAttachments - success, empty list")
    void getAttachments_empty() {
        when(taskAttachmentRepository.findByTaskId(999L)).thenReturn(List.of());

        List<AttachmentResponse> result = taskAttachmentService.getAttachments(999L);

        assertThat(result).isEmpty();
    }
}