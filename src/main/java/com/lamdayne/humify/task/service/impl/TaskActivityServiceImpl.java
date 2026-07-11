package com.lamdayne.humify.task.service.impl;

import com.lamdayne.humify.task.dto.response.ActivityResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskActivity;
import com.lamdayne.humify.task.mapper.TaskActivityMapper;
import com.lamdayne.humify.task.repository.TaskActivityRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.task.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskRepository taskRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final TaskActivityMapper taskActivityMapper;

    @Override
    public List<ActivityResponse> getActivities(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found"));

        List<TaskActivity> activities =
                taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(task.getId());

        return activities.stream()
                .map(taskActivityMapper::toResponse)
                .toList();
    }
}