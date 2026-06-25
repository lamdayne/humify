package com.lamdayne.humify.project.dto.response;

import com.lamdayne.humify.project.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
public class ProjectResponse {

    private Long id;

    private Long companyId;

    private Long creatorId;

    private String name;

    private String key;

    private String description;

    private ProjectStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}