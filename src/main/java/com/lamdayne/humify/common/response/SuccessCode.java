package com.lamdayne.humify.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SuccessCode {

    // Auth
    LOGIN_SUCCESS("LOGIN_SUCCESS", "Login Success"),
    REFRESH_TOKEN_SUCCESS("REFRESH_TOKEN_SUCCESS", "Refresh Token Success"),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS", "Logout Success"),

    // Company
    COMPANY_CREATE_SUCCESS("COMPANY_CREATE_SUCCESS", "Company created successfully"),
    COMPANY_READ_SUCCESS("COMPANY_READ_SUCCESS", "Get company successfully"),
    APPROVE_COMPANY_SUCCESS("APPROVE_COMPANY_SUCCESS", "Approve company successfully"),
    COMPANY_UPDATE_SUCCESS("COMPANY_UPDATE_SUCCESS", "Update company successfully"),

    // Department
    DEPARTMENT_CREATE_SUCCESS("DEPARTMENT_CREATE_SUCCESS", "Department created successfully" ),
    FOUND_DEPARTMENT_SUCCESS("FOUND_DEPARTMENT_SUCCESS", "Department found successfully"),

    // Branch
    BRANCH_CREATE_SUCCESS("BRANCH_CREATE_SUCCESS", "Branch created successfully"),
    FOUND_BRANCH_SUCCESS("FOUND_BRANCH_SUCCESS", "Branch found successfully"),

    // Role
    ROLE_CREATE_SUCCESS("ROLE_CREATE_SUCCESS", "Role created successfully"),
    ROLE_DELETE_SUCCESS("ROLE_DELETE_SUCCESS", "Role deleted successfully"),

    ;

    private String code;
    private String defaultMessage;
}
