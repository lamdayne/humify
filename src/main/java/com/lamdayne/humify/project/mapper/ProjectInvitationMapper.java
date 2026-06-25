package com.lamdayne.humify.project.mapper;

import com.lamdayne.humify.project.dto.response.InvitationResponse;
import com.lamdayne.humify.project.dto.response.ValidateInvitationResponse;
import com.lamdayne.humify.project.entity.ProjectInvitation;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectInvitationMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "projectRole.id", target = "projectRoleId")
    @Mapping(source = "inviter.id", target = "inviterId")
    @Mapping(source = "expiredAt", target = "expiresAt")
    @Mapping(target = "inviteLink", ignore = true)
    InvitationResponse toInvitationResponse(ProjectInvitation invitation);

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "projectRole.code", target = "roleCode")
    @Mapping(target = "roleName", ignore = true)
    @Mapping(target = "isValid", ignore = true)
    ValidateInvitationResponse toValidateInvitationResponse(ProjectInvitation invitation);

    @AfterMapping
    default void mapAfter(ProjectInvitation invitation, @MappingTarget ValidateInvitationResponse response) {
        if (invitation.getProjectRole() != null) {
            String code = invitation.getProjectRole().getCode();
            if ("MEMBER".equals(code)) {
                response.setRoleName("Project Member");
            } else if ("MANAGER".equals(code)) {
                response.setRoleName("Project Manager");
            } else if ("VIEWER".equals(code)) {
                response.setRoleName("Project Viewer");
            } else {
                response.setRoleName(invitation.getProjectRole().getName());
            }
        }
        response.setValid(true);
    }
}
