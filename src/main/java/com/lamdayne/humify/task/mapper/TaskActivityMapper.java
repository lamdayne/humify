package com.lamdayne.humify.task.mapper;

import com.lamdayne.humify.task.dto.response.ActivityResponse;
import com.lamdayne.humify.task.dto.response.ActivityUserResponse;
import com.lamdayne.humify.task.entity.TaskActivity;
import org.springframework.stereotype.Component;

@Component
public class TaskActivityMapper {

    public ActivityResponse toResponse(TaskActivity activity) {

        return ActivityResponse.builder()
                .id(activity.getId())
                .taskId(activity.getTask().getId())
                .action(activity.getAction().name())
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .createdAt(activity.getCreatedAt())
                .user(
                        ActivityUserResponse.builder()
                                .id(activity.getUser().getId())
                                .fullName(activity.getUser()
                                        .getEmployee()
                                        .getFullName())
                                .build()
                )
                .build();
    }
}