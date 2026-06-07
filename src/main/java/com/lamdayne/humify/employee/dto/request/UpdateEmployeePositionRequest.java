package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class UpdateEmployeePositionRequest implements Serializable {

    @NotNull(message = "POSITION_ID_REQUIRED")
    @Positive(message = "POSITION_ID_INVALID")
    private Long positionId;

}
