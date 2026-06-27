package com.lamdayne.humify.task.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.task.dto.request.CreateWorklogRequest;
import com.lamdayne.humify.task.dto.request.UpdateWorklogRequest;
import com.lamdayne.humify.task.dto.response.WorklogResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskWorkLog;
import com.lamdayne.humify.task.mapper.TaskWorkLogMapper;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.repository.TaskWorkLogRepository;
import com.lamdayne.humify.task.service.TaskWorkLogService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class TaskWorkLogServiceImpl implements TaskWorkLogService {

    private final TaskWorkLogMapper taskWorkLogMapper;
    private final TaskWorkLogRepository taskWorkLogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;



    @Override
    public WorklogResponse create(Long taskId, CreateWorklogRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // vì bạn dùng email làm login
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        TaskWorkLog workLog = TaskWorkLog.builder()
                .task(task)
                .user(user)
                .timeSpentHours(request.getTimeSpentHours())
                .description(request.getDescription())
                .loggedAt(request.getLoggedAt())
                .build();

        taskWorkLogRepository.save(workLog);

        task.setLoggedHours(task.getLoggedHours() + request.getTimeSpentHours());

        taskRepository.save(task);

        return taskWorkLogMapper.toResponse(workLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorklogResponse> getByTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new AppException(ErrorCode.TASK_NOT_FOUND));

        return taskWorkLogRepository.findByTaskIdOrderByLoggedAtDesc(task.getId())
                .stream()
                .map(taskWorkLogMapper::toResponse)
                .toList();
    }

    @Override
    public WorklogResponse update(Long id, UpdateWorklogRequest request) {
        TaskWorkLog workLog =taskWorkLogRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.WORKLOG_NOT_FOUND));
        double diff = request.getTimeSpentHours() - workLog.getTimeSpentHours();
        Task task = workLog.getTask();
        task.setLoggedHours(task.getLoggedHours() + diff);
        workLog.setTimeSpentHours(request.getTimeSpentHours());
        workLog.setDescription(request.getDescription());
        workLog.setLoggedAt(request.getLoggedAt());

        taskWorkLogRepository.save(workLog);
        taskRepository.save(task);
        return taskWorkLogMapper.toResponse(workLog);
    }

    @Override
    public void delete(Long id) {
        TaskWorkLog workLog = taskWorkLogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WORKLOG_NOT_FOUND));
        Task task = workLog.getTask();
        task.setLoggedHours(
                task.getLoggedHours() - workLog.getTimeSpentHours()
        );

        taskWorkLogRepository.delete(workLog);

        taskRepository.save(task);
    }
}
