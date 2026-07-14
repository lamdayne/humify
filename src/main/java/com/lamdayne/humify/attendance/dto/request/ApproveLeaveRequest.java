package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveLeaveRequest {

    @NotBlank(message = "LEAVE_REQUEST_APPROVER_NOTE_REQUIRED")
    private String approverNote;

}
