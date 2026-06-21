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
    FORGOT_PASSWORD_SUCCESS("FORGOT_PASSWORD_SUCCESS", "Forgot Password"),
    RESET_PASSWORD_SUCCESS("RESET_PASSWORD_SUCCESS", "Reset Password Success"),
    VERIFY_COMPANY_SUCCESS("VERIFY_COMPANY_SUCCESS", "Verify Company Success"),
    RESEND_EMAIL_SUCCESS("RESEND_EMAIL_SUCCESS", "Resend Email Success"),
    GET_MY_INFO_SUCCESS("GET_MY_INFO_SUCCESS", "Get My Info Success"),

    // Company
    COMPANY_CREATE_SUCCESS("COMPANY_CREATE_SUCCESS", "Company created successfully"),
    COMPANY_READ_SUCCESS("COMPANY_READ_SUCCESS", "Get company successfully"),
    APPROVE_COMPANY_SUCCESS("APPROVE_COMPANY_SUCCESS", "Approve company successfully"),
    COMPANY_UPDATE_SUCCESS("COMPANY_UPDATE_SUCCESS", "Update company successfully"),

    // Department
    DEPARTMENT_CREATE_SUCCESS("DEPARTMENT_CREATE_SUCCESS", "Department created successfully" ),
    FOUND_DEPARTMENT_SUCCESS("FOUND_DEPARTMENT_SUCCESS", "Department found successfully"),
    DEPARTMENT_UPDATE_SUCCESS("DEPARTMENT_UPDATE_SUCCESS", "Department update successfully"),
    // Branch
    BRANCH_CREATE_SUCCESS("BRANCH_CREATE_SUCCESS", "Branch created successfully"),
    FOUND_BRANCH_SUCCESS("FOUND_BRANCH_SUCCESS", "Branch found successfully"),

    // Position
    POSITION_CREATE_SUCCESS("POSITION_CREATE_SUCCESS", "Position created successfully"),
    FOUND_POSITION_SUCCESS("FOUND_POSITION_SUCCESS", "Get positions data successfully"),

    // Role
    ROLE_CREATE_SUCCESS("ROLE_CREATE_SUCCESS", "Role created successfully"),
    ROLE_DELETE_SUCCESS("ROLE_DELETE_SUCCESS", "Role deleted successfully"),
    ROLE_UPDATE_SUCCESS("ROLE_UPDATE_SUCCESS", "Role updated successfully"),
    ROLE_READ_ALL_SUCCESS("ROLE_READ_ALL_SUCCESS", "Get all role successfully"),
    ROLE_READ_SUCCESS("ROLE_READ_SUCCESS", "Get role successfully"),

    // Permission
    PERMISSION_READ_SUCCESS("PERMISSION_READ_SUCCESS", "Get permissions successfully"),

    // Employee
    EMPLOYEE_CREATE_SUCCESS("EMPLOYEE_CREATE_SUCCESS", "Employee created successfully"),
    EMPLOYEE_READ_SUCCESS("EMPLOYEE_READ_SUCCESS", "Employee read successfully"),
    EMPLOYEE_UPDATE_SUCCESS("EMPLOYEE_UPDATE_SUCCESS", "Employee updated successfully"),
    EMPLOYEE_TRANSFER_SUCCESS("EMPLOYEE_TRANSFER_SUCCESS", "Employee transfer successfully"),
    EMPLOYEE_POSITION_UPDATE_SUCCESS("EMPLOYEE_POSITION_UPDATE_SUCCESS", "Employee position updated successfully"),
    EMPLOYEE_STATUS_UPDATE_SUCCESS("EMPLOYEE_STATUS_UPDATE_SUCCESS", "Employee status updated successfully"),

    // User
    USER_CREATE_SUCCESS("USER_CREATE_SUCCESS", "User created successfully"),
    USER_READ_SUCCESS("USER_READ_SUCCESS", "Get all user successfully"),
    USER_CHANGE_ROLE_SUCCESS("USER_CHANGE_ROLE_SUCCESS", "Change role successfully"),
    USER_CHANGE_PASSWORD_SUCCESS("USER_CHANGE_PASSWORD_SUCCESS", "Change password successfully");



    private String code;
    private String defaultMessage;
}
