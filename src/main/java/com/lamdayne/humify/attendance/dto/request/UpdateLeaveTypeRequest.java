package com.lamdayne.humify.attendance.dto.request;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLeaveTypeRequest {

    @Size(max = 100, message = "LEAVE_TYPE_NAME_MAX_LENGTH")
    private String name;

    @Pattern(regexp = "^[A-Z_]+$", message = "LEAVE_TYPE_CODE_INVALID")
    private String code;

    private Integer maxDays;
    private Boolean isPaid;
    private Boolean requiresAttachment;
    private String description;


}
