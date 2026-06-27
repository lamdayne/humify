package com.lamdayne.humify.task.mapper;

import com.lamdayne.humify.task.dto.response.TaskResponse;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface TaskMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.key", target = "projectKey")
    @Mapping(source = "sprint.id", target = "sprintId")
    @Mapping(source = "column.id", target = "columnId")
    @Mapping(source = "parent.id", target = "parentId")
    TaskResponse toResponse(Task task);

}
