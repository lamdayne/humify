package com.lamdayne.humify.project.mapper;

import com.lamdayne.humify.project.dto.response.MemberInfo;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.dto.response.ProjectRoleResponse;
import com.lamdayne.humify.project.entity.ProjectMember;
import com.lamdayne.humify.project.entity.ProjectRole;
import com.lamdayne.humify.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "projectRole", target = "role")
    ProjectMemberResponse toProjectMemberResponse(ProjectMember projectMember);

    @Mapping(source = "employee.fullName", target = "fullName")
    MemberInfo toMemberInfo(User user);

    ProjectRoleResponse toProjectRoleResponse(ProjectRole projectRole);

}
