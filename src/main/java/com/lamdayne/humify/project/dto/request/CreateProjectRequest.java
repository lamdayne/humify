package com.lamdayne.humify.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {
    @NotBlank(message = "PROJECT_NAME_REQUIRED")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "PROJECT_KEY_REQUIRED")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$" ,message = "PROJECT_KEY_INVALID")

    private String key;

    @Size(max = 1000)
    private String description;
    private Long companyId;

    private Long creatorId;
}
