package com.lamdayne.humify.task.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserShortResponse {
    private Long id;
    private String email;
}
