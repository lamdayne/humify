package com.lamdayne.humify.project.mapper;


import com.lamdayne.humify.project.dto.request.CreateProjectRequest;
import com.lamdayne.humify.project.dto.request.UpdateProjectRequest;
import com.lamdayne.humify.project.dto.response.ProjectResponse;
import com.lamdayne.humify.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "status", ignore = true)
    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "creatorId", source = "creator.id")
    ProjectResponse toResponse(Project project) ;


    @Mapping(target = "company", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "key", ignore = true)

    void updateEntity(
            UpdateProjectRequest request,
            @MappingTarget Project project
    );
}
