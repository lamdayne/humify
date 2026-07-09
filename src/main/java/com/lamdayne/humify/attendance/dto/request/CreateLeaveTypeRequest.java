package com.lamdayne.humify.attendance.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateLeaveTypeRequest {

    @NotBlank(message = "LEAVE_TYPE_NAME_REQUIRED")
    @Size(max = 100, message = "LEAVE_TYPE_NAME_MAX_LENGTH")
    private String name;

    @NotBlank(message = "LEAVE_TYPE_CODE_REQUIRED")
    @Pattern(regexp = "^[A-Z_]+$", message = "LEAVE_TYPE_CODE_INVALID")
    private String code;

    private BigDecimal maxDays;

    private Boolean isPaid;

    private Boolean requiresAttachment;

    private String description;
}
