package com.lamdayne.humify.project.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberInfo {
    private Long id;
    private String email;
    private String fullName;
}
