package com.lamdayne.humify.position.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CreatePositionRequest implements Serializable {

    @NotBlank(message = "POSITION_NAME_REQUIRED")
    private String name;

    private String description;
}