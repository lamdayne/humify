package com.lamdayne.humify.project.mapper;


import com.lamdayne.humify.project.dto.response.SprintResponse;
import com.lamdayne.humify.project.entity.Sprint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(source = "project.id", target = "projectId")
    SprintResponse toResponse(Sprint sprint);
}
