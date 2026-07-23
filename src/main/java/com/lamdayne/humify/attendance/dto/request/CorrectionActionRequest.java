package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorrectionActionRequest {

    @NotBlank(message = "APPROVER_NOTE_REQUIRED")
    private String approverNote;
}
