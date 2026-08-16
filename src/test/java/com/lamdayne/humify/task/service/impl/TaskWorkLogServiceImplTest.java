package com.lamdayne.humify.task.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.task.dto.request.CreateWorklogRequest;
import com.lamdayne.humify.task.dto.request.UpdateWorklogRequest;
import com.lamdayne.humify.task.dto.response.UserSummaryResponse;
import com.lamdayne.humify.task.dto.response.WorklogResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskWorkLog;
import com.lamdayne.humify.task.mapper.TaskWorkLogMapper;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.repository.TaskWorkLogRepository;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskWorkLogServiceImplTest {

    @Mock
    private TaskWorkLogRepository taskWorkLogRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskWorkLogMapper taskWorkLogMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskWorkLogServiceImpl taskWorkLogService;

    private Task task;
    private User user;
    private TaskWorkLog workLog;
    private CreateWorklogRequest createRequest;
    private UpdateWorklogRequest updateRequest;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);

        task = mock(Task.class);
        user = mock(User.class);

        workLog = TaskWorkLog.builder()
                .id(1L)
                .task(task)
                .user(user)
                .timeSpentHours(2.0)
                .description("Test worklog")
                .loggedAt(Instant.now())
                .build();

        createRequest = new CreateWorklogRequest();
        createRequest.setTimeSpentHours(2.0);
        createRequest.setDescription("Test worklog");
        createRequest.setLoggedAt(Instant.now());

        updateRequest = new UpdateWorklogRequest();
        updateRequest.setTimeSpentHours(3.0);
        updateRequest.setDescription("Updated worklog");
        updateRequest.setLoggedAt(Instant.now());
    }

    // =========================================================
    // create()
    // =========================================================

    @Test
    void create_success() {
        Long taskId = 1L;

        WorklogResponse response = WorklogResponse.builder()
                .id(1L)
                .taskId(taskId)
                .timeSpentHours(2.0)
                .description("Test worklog")
                .build();

        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(taskWorkLogRepository.save(any(TaskWorkLog.class)))
                .thenReturn(workLog);

        when(task.getLoggedHours()).thenReturn(0.0);
        when(taskRepository.save(task)).thenReturn(task);

        when(taskWorkLogMapper.toResponse(any(TaskWorkLog.class)))
                .thenReturn(response);

        WorklogResponse result =
                taskWorkLogService.create(taskId, createRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(taskId, result.getTaskId());
        assertEquals(2.0, result.getTimeSpentHours());

        verify(taskRepository).findById(taskId);
        verify(userRepository).findByEmail("test@example.com");
        verify(taskWorkLogRepository).save(any(TaskWorkLog.class));
        verify(taskRepository).save(task);
        verify(taskWorkLogMapper).toResponse(any(TaskWorkLog.class));
    }
    @Test
    void create_taskNotFound() {
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> taskWorkLogService.create(taskId, createRequest)
        );

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());

        verify(taskRepository).findById(taskId);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void create_userNotFound() {
        Long taskId = 1L;

        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> taskWorkLogService.create(taskId, createRequest)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        verify(taskRepository).findById(taskId);
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(taskWorkLogRepository, never()).save(any());
    }

    @Test
    void create_updatesTaskLoggedHours() {
        Long taskId = 1L;
        double initialLoggedHours = 5.0;

        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(taskWorkLogRepository.save(any(TaskWorkLog.class))).thenReturn(workLog);
        when(task.getLoggedHours()).thenReturn(initialLoggedHours);
        when(taskRepository.save(task)).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });
        when(taskWorkLogMapper.toResponse(any(TaskWorkLog.class))).thenReturn(
                WorklogResponse.builder().id(1L).build()
        );

        taskWorkLogService.create(taskId, createRequest);

        verify(task).setLoggedHours(initialLoggedHours + 2.0);
    }

    // =========================================================
    // getByTask()
    // =========================================================

    @Test
    void getByTask_success() {
        Long taskId = 1L;
        when(task.getId()).thenReturn(taskId);
        TaskWorkLog workLog2 = TaskWorkLog.builder()
                .id(2L)
                .task(task)
                .user(user)
                .timeSpentHours(1.5)
                .description("Second worklog")
                .loggedAt(Instant.now())
                .build();

        WorklogResponse response1 = WorklogResponse.builder()
                .id(1L)
                .taskId(taskId)
                .timeSpentHours(2.0)
                .build();

        WorklogResponse response2 = WorklogResponse.builder()
                .id(2L)
                .taskId(taskId)
                .timeSpentHours(1.5)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskWorkLogRepository.findByTaskIdOrderByLoggedAtDesc(taskId))
                .thenReturn(List.of(workLog, workLog2));
        when(taskWorkLogMapper.toResponse(workLog)).thenReturn(response1);
        when(taskWorkLogMapper.toResponse(workLog2)).thenReturn(response2);

        List<WorklogResponse> result = taskWorkLogService.getByTask(taskId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(taskRepository).findById(taskId);
        verify(taskWorkLogRepository).findByTaskIdOrderByLoggedAtDesc(taskId);
    }

    @Test
    void getByTask_emptyList() {
        Long taskId = 1L;
        when(task.getId()).thenReturn(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskWorkLogRepository.findByTaskIdOrderByLoggedAtDesc(taskId))
                .thenReturn(List.of());

        List<WorklogResponse> result = taskWorkLogService.getByTask(taskId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findById(taskId);
        verify(taskWorkLogRepository).findByTaskIdOrderByLoggedAtDesc(taskId);
    }

    @Test
    void getByTask_taskNotFound() {
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> taskWorkLogService.getByTask(taskId)
        );

        assertEquals(ErrorCode.TASK_NOT_FOUND, exception.getErrorCode());

        verify(taskRepository).findById(taskId);
        verify(taskWorkLogRepository, never()).findByTaskIdOrderByLoggedAtDesc(anyLong());
    }

    // =========================================================
    // update()
    // =========================================================

    @Test
    void update_success() {
        Long worklogId = 1L;

        WorklogResponse response = WorklogResponse.builder()
                .id(worklogId)
                .taskId(1L)
                .timeSpentHours(3.0)
                .description("Updated worklog")
                .build();

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.of(workLog));
        when(task.getLoggedHours()).thenReturn(10.0);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskWorkLogRepository.save(workLog)).thenReturn(workLog);
        when(taskWorkLogMapper.toResponse(workLog)).thenReturn(response);

        WorklogResponse result = taskWorkLogService.update(worklogId, updateRequest);

        assertNotNull(result);
        assertEquals(worklogId, result.getId());

        verify(taskWorkLogRepository).findById(worklogId);
        verify(taskWorkLogRepository).save(workLog);
        verify(taskRepository).save(task);
    }

    @Test
    void update_correctLoggedHoursDifference() {
        Long worklogId = 1L;

        // Initial: worklog has 2.0 hours, task has 10.0 hours
        // Update to: 3.0 hours, diff = 3.0 - 2.0 = 1.0
        // Expected: task should have 10.0 + 1.0 = 11.0 hours

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.of(workLog));
        when(task.getLoggedHours()).thenReturn(10.0);
        when(taskRepository.save(task)).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });
        when(taskWorkLogRepository.save(workLog)).thenReturn(workLog);
        when(taskWorkLogMapper.toResponse(workLog)).thenReturn(
                WorklogResponse.builder().id(worklogId).build()
        );

        taskWorkLogService.update(worklogId, updateRequest);

        verify(task).setLoggedHours(11.0);
    }

    @Test
    void update_negativeDifference() {
        Long worklogId = 1L;
        updateRequest.setTimeSpentHours(1.0); // decrease from 2.0 to 1.0

        // Initial: worklog has 2.0 hours, task has 10.0 hours
        // Update to: 1.0 hours, diff = 1.0 - 2.0 = -1.0
        // Expected: task should have 10.0 - 1.0 = 9.0 hours

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.of(workLog));
        when(task.getLoggedHours()).thenReturn(10.0);
        when(taskRepository.save(task)).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });
        when(taskWorkLogRepository.save(workLog)).thenReturn(workLog);
        when(taskWorkLogMapper.toResponse(workLog)).thenReturn(
                WorklogResponse.builder().id(worklogId).build()
        );

        taskWorkLogService.update(worklogId, updateRequest);

        verify(task).setLoggedHours(9.0);
    }

    @Test
    void update_worklogNotFound() {
        Long worklogId = 999L;

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> taskWorkLogService.update(worklogId, updateRequest)
        );

        assertEquals(ErrorCode.WORKLOG_NOT_FOUND, exception.getErrorCode());

        verify(taskWorkLogRepository).findById(worklogId);
        verify(taskWorkLogRepository, never()).save(any());
    }

    // =========================================================
    // delete()
    // =========================================================

    @Test
    void delete_success() {
        Long worklogId = 1L;

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.of(workLog));
        when(task.getLoggedHours()).thenReturn(10.0);
        when(taskRepository.save(task)).thenReturn(task);

        taskWorkLogService.delete(worklogId);

        verify(taskWorkLogRepository).findById(worklogId);
        verify(task).setLoggedHours(8.0); // 10.0 - 2.0
        verify(taskWorkLogRepository).delete(workLog);
        verify(taskRepository).save(task);
    }

    @Test
    void delete_updatesTaskLoggedHours() {
        Long worklogId = 1L;

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.of(workLog));
        when(task.getLoggedHours()).thenReturn(5.0);
        when(taskRepository.save(task)).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });

        taskWorkLogService.delete(worklogId);

        verify(task).setLoggedHours(3.0); // 5.0 - 2.0
        verify(taskWorkLogRepository).delete(workLog);
    }

    @Test
    void delete_worklogNotFound() {
        Long worklogId = 999L;

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> taskWorkLogService.delete(worklogId)
        );

        assertEquals(ErrorCode.WORKLOG_NOT_FOUND, exception.getErrorCode());

        verify(taskWorkLogRepository).findById(worklogId);
        verify(taskWorkLogRepository, never()).delete(any());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void delete_doesNotAffectOtherWorklogs() {
        Long worklogId = 1L;

        TaskWorkLog workLog2 = TaskWorkLog.builder()
                .id(2L)
                .task(task)
                .user(user)
                .timeSpentHours(1.5)
                .build();

        when(taskWorkLogRepository.findById(worklogId)).thenReturn(Optional.of(workLog));
        when(task.getLoggedHours()).thenReturn(5.0);
        when(taskRepository.save(task)).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            return savedTask;
        });

        taskWorkLogService.delete(worklogId);

        verify(task).setLoggedHours(3.0); // 5.0 - 2.0
        verify(taskWorkLogRepository).delete(workLog);
        verify(taskWorkLogRepository, never()).delete(workLog2);
    }

}