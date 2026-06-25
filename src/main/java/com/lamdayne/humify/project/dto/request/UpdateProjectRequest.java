package com.lamdayne.humify.project.dto.request;

import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProjectRequest {

    @NotBlank(message = "PROJECT_NAME_REQUIRED")
    private String name;

    private String description;

    @EnumValue(name = "project_status", message = "PROJECT_STATUS_INVALID", enumClass = ProjectStatus.class)
    private String status;

}
