package com.lamdayne.humify.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginResposne {
    private String loginUrl;
}
