package com.lamdayne.humify.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lamdayne.humify.auth.entity.Permission;
import com.lamdayne.humify.auth.entity.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private List<PermissionResponse> permissions;

    public static RoleResponse from(Role role, List<Permission> permissions) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(permissions.stream()
                        .map(PermissionResponse::from)
                        .toList()
                )
                .build();
    }
}
