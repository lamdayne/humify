package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceApprovalRequest {
    @NotBlank(message = "Ghi chú phê duyệt/từ chối không được để trống")
    private String approverNote;
}