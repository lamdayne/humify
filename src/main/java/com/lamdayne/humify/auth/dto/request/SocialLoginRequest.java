package com.lamdayne.humify.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest implements Serializable {

    @NotBlank(message = "INVALID_CODE")
    private String code;

    private String companyCode;

}
