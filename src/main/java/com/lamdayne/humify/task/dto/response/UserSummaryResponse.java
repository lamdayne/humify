package com.lamdayne.humify.task.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class UserSummaryResponse {
    private Long id;

    private String fullName;
}
