package com.lamdayne.humify.position.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class PositionRequest implements Serializable {

    @NotNull(message = "COMPANY_ID_REQUIRED")
    private Long companyId;

    @NotBlank(message = "POSITION_NAME_REQUIRED")
    private String name;

    private String description;

}