package com.lamdayne.humify.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SuccessCode {
    COMPANY_CREATE_SUCCESS("COMPANY_CREATE_SUCCESS", "Company created successfully"),
    ;

    private String code;
    private String defaultMessage;
}
