package com.lamdayne.humify.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectRoleResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
}
