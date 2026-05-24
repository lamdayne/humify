package com.lamdayne.humify.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SuccessCode {
    CREATE_COMPANY_SUCCESS("CREATE_COMPANY_SUCCESS", "Create company success"),
    ;

    private String code;
    private String defaultMessage;
}
