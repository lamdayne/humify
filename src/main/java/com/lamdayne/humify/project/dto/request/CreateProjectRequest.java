package com.lamdayne.humify.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {

    @NotBlank(message = "PROJECT_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "PROJECT_KEY_REQUIRED")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$" ,message = "PROJECT_KEY_INVALID")
    private String key;

    private String description;
}
