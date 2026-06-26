package com.lamdayne.humify.task.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssigneeInfo {
    private Long id;
    private String fullName;
}
