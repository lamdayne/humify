package com.lamdayne.humify.project.dto.request;

import com.lamdayne.humify.common.validator.EnumValue;
import com.lamdayne.humify.project.enums.SprintStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSprintStatusRequest {
    @EnumValue(name = "SprintStatus", message = "SPRINT_STATUS_INVALID", enumClass = SprintStatus.class)
    private String status;
}
