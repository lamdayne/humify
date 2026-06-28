package com.lamdayne.humify.task.mapper;


import com.lamdayne.humify.task.dto.response.CommentResponse;
import com.lamdayne.humify.task.entity.TaskComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskCommentMapper {

    @Mapping(source = "task.id", target = "taskId")
    @Mapping(source = "parent.id", target = "parentId")
    CommentResponse toResponse(TaskComment comment);
}
