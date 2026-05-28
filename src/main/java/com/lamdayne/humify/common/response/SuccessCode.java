package com.lamdayne.humify.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SuccessCode {
    // Company
    COMPANY_CREATE_SUCCESS("COMPANY_CREATE_SUCCESS", "Company created successfully"),
    COMPANY_READ_SUCCESS("COMPANY_READ_SUCCESS", "Get company successfully"),
    APPROVE_COMPANY_SUCCESS("APPROVE_COMPANY_SUCCESS", "Approve company successfully"),
    COMPANY_UPDATE_SUCCESS("COMPANY_UPDATE_SUCCESS", "Update company successfully"),

    // Department
    DEPARTMENT_CREATE_SUCCESS("DEPARTMENT_CREATE_SUCCESS", "Department created successfully" ),
    FOUND_DEPARTMENT_SUCCESS("FOUND_DEPARTMENT_SUCCES", "Department found successfully"),

    // Branch
    BRANCH_CREATE_SUCCESS("BRANCH_CREATE_SUCCESS", "Branch created successfully"),
    FOUND_BRANCH_SUCCESS("FOUND_BRANCH_SUCCESS", "Branch found successfully"),;

    private String code;
    private String defaultMessage;
}
