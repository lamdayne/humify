package com.lamdayne.humify.task.mapper;

import com.lamdayne.humify.task.dto.response.WorklogResponse;
import com.lamdayne.humify.task.entity.TaskWorkLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskWorkLogMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "user.fullName", source = "user.employee.fullName")
    WorklogResponse toResponse(TaskWorkLog workLog);
}
