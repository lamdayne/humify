package com.lamdayne.humify.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lamdayne.humify.auth.entity.Permission;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
    private String module;
    private Boolean canAssign;

    public static PermissionResponse from(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .module(permission.getModule())
                .build();
    }
}
