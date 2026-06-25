package com.lamdayne.humify.project.dto.request;

import com.lamdayne.humify.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectRequest {
    @NotBlank
    private String name;

    private String description;

    private ProjectStatus status;
}
