package com.lamdayne.humify.task.mapper;

import com.lamdayne.humify.task.dto.response.ActivityResponse;
import com.lamdayne.humify.task.dto.response.ActivityUserResponse;
import com.lamdayne.humify.task.entity.TaskActivity;
import com.lamdayne.humify.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskActivityMapper {

    public ActivityResponse toResponse(TaskActivity activity) {
        if (activity == null) {
            return null;
        }

        User user = activity.getUser();
        String fullName = null;
        String email = null;

        if (user != null) {
            email = user.getEmail();
            if (user.getEmployee() != null) {
                fullName = user.getEmployee().getFullName();
            } else {
                fullName = user.getEmail();
            }
        }

        return ActivityResponse.builder()
                .id(activity.getId())
                .taskId(activity.getTask() != null ? activity.getTask().getId() : null)
                .action(activity.getAction() != null ? activity.getAction().name() : null)
                .oldValue(activity.getOldValue())
                .newValue(activity.getNewValue())
                .createdAt(activity.getCreatedAt())
                .user(user != null ? ActivityUserResponse.builder()
                        .id(user.getId())
                        .email(email)
                        .fullName(fullName)
                        .build() : null)
                .build();
    }
}