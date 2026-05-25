package com.lamdayne.humify.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SuccessCode {
    COMPANY_CREATE_SUCCESS("COMPANY_CREATE_SUCCESS", "Company created successfully"),
    COMPANY_READ_SUCCESS("COMPANY_READ_SUCCESS", "Get company successfully"),
    DEPARTMENT_CREATE_SUCCESS("DEPARTMENT_CREATE_SUCCESS", "Department created successfully" ),
    FOUND_DEPARTMENT_SUCCESS("FOUND_DEPARTMENT_SUCCES", "Department found successfully");

    private String code;
    private String defaultMessage;
}
