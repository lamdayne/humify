package com.lamdayne.humify.task.mapper;

import com.lamdayne.humify.task.dto.request.UpdateTaskRequest;
import com.lamdayne.humify.task.dto.response.AttachmentResponse;
import com.lamdayne.humify.task.dto.response.SubtaskResponse;
import com.lamdayne.humify.task.dto.response.TaskDetailResponse;
import com.lamdayne.humify.task.dto.response.TaskResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskAttachment;
import com.lamdayne.humify.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface TaskMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    @Mapping(source = "sprint.id", target = "sprintId")
    @Mapping(source = "column.id", target = "columnId")
    @Mapping(source = "parent.id", target = "parentId")
    TaskResponse toResponse(Task task);

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    @Mapping(source = "sprint.id", target = "sprintId")
    @Mapping(source = "column.id", target = "columnId")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(target = "subtasks", ignore = true)
    TaskDetailResponse toDetailResponse(Task task);

    @Mapping(source = "column.id", target = "columnId")
    @Mapping(source = "completedAt", target = "completedAt")
    SubtaskResponse toSubtaskResponse(Task task);

    void updateTask(@MappingTarget Task task, UpdateTaskRequest request);

    @Mapping(source = "task.id", target = "taskId")
    AttachmentResponse toAttachmentResponse(TaskAttachment attachment);

}
