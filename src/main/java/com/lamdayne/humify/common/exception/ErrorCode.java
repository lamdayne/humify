package com.lamdayne.humify.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {

    // Common
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    INVALID_ERROR_CODE("INVALID_ERROR_CODE", "Invalid error code", HttpStatus.BAD_REQUEST),
    PAGE_NO_INVALID("PAGE_NO_INVALID", "Invalid page number", HttpStatus.BAD_REQUEST),
    PAGE_SIZE_INVALID("PAGE_SIZE_INVALID", "Invalid page size", HttpStatus.BAD_REQUEST),
    INVALID_FIELD_NAME("INVALID_FIELD_NAME", "Invalid field name", HttpStatus.BAD_REQUEST),
    INVALID_CODE("INVALID_CODE", "Invalid code", HttpStatus.BAD_REQUEST),
    FORBIDDEN("FORBIDDEN", "Forbidden", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found", HttpStatus.NOT_FOUND),
    REQUEST_BODY_MISSING_OR_INVALID("REQUEST_BODY_MISSING_OR_INVALID", "Body missing or invalid", HttpStatus.BAD_REQUEST),

    // Branch
    COMPANY_ID_REQUIRED("COMPANY_ID_REQUIRED", "Company ID cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_REQUIRED("BRANCH_CODE_REQUIRED", "Branch code cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_INVALID("BRANCH_CODE_INVALID", "Branch code exceed max length", HttpStatus.BAD_REQUEST),
    BRANCH_NAME_REQUIRED("BRANCH_NAME_REQUIRED", "Branch name cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_FIELD_REQUIRED("BRANCH_FIELD_REQUIRED", "Branch field cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_EXISTED("BRANCH_CODE_EXISTED", "Branch code already exists in this company", HttpStatus.BAD_REQUEST),
    BRANCH_ID_INVALID("BRANCH_ID_INVALID", "Branch ID is invalid", HttpStatus.BAD_REQUEST),

    // Position
    POSITION_NAME_REQUIRED("POSITION_NAME_REQUIRED", "Position name cannot be blank", HttpStatus.BAD_REQUEST),
    POSITION_NOT_FOUND("POSITION_NOT_FOUND", "Position resource not found", HttpStatus.BAD_REQUEST),
    POSITION_ID_REQUIRED("POSITION_ID_REQUIRED", "Position ID cannot be blank", HttpStatus.BAD_REQUEST),
    POSITION_ID_INVALID("POSITION_ID_INVALID", "Position ID is invalid", HttpStatus.BAD_REQUEST),

    // Company
    COMPANY_NAME_REQUIRED("COMPANY_NAME_REQUIRED", "Company name can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_FIELD_REQUIRED("COMPANY_FIELD_REQUIRED", "Company field can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_TAX_CODE_REQUIRED("COMPANY_TAX_CODE_REQUIRED", "Company tax code can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_PHONE_REQUIRED("COMPANY_PHONE_REQUIRED", "Company phone can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_EMAIL_REQUIRED("COMPANY_EMAIL_REQUIRED", "Company email can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_TAX_CODE_INVALID("COMPANY_TAX_CODE_INVALID", "Company tax code is invalid", HttpStatus.BAD_REQUEST),
    COMPANY_TAX_CODE_EXISTED("COMPANY_TAX_CODE_EXISTED", "Company tax code already exists", HttpStatus.BAD_REQUEST),
    COMPANY_PHONE_INVALID("COMPANY_PHONE_INVALID", "Company phone number is invalid", HttpStatus.BAD_REQUEST),
    COMPANY_EMAIL_INVALID("COMPANY_EMAIL_INVALID", "Company email is invalid", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_FOUND("COMPANY_NOT_FOUND", "Company not found", HttpStatus.BAD_REQUEST),
    COMPANY_EMAIL_EXISTED("COMPANY_EMAIL_EXISTED", "Company email already exists", HttpStatus.BAD_REQUEST),

    //Department
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Branch not found" , HttpStatus.BAD_REQUEST ),
    BRANCH_ID_REQUIRED("BRANCH_ID_REQUIRED", "Branch id can not blank", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NAME_REQUIRED("DEPARTMENT_NAME_REQUIRED", "Department name can not blank", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NOT_FOUND("DEPARTMENT_NOT_FOUND", "Department resource not found", HttpStatus.NOT_FOUND),
    DEPARTMENT_ID_REQUIRED("DEPARTMENT_ID_REQUIRED", "Department ID cannot be blank", HttpStatus.BAD_REQUEST),
    DEPARTMENT_ID_INVALID("DEPARTMENT_ID_INVALID", "Department ID is invalid", HttpStatus.BAD_REQUEST),

    // User
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.BAD_REQUEST),
    USER_EMAIL_REQUIRED("USER_EMAIL_REQUIRED", "Email is required", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_REQUIRED("USER_PASSWORD_REQUIRED", "Password is required", HttpStatus.BAD_REQUEST),

    // Auth
    JWT_EXPIRED("JWT_EXPIRED", "Token expired", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthorized", HttpStatus.UNAUTHORIZED),
    EMAIL_REQUIRED("EMAIL_REQUIRED", "Email can not blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID("EMAIL_INVALID", "Email address is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("PASSWORD_REQUIRED", "Password cannot be blank", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.BAD_REQUEST),

    // Role
    ROLE_NAME_REQUIRED("ROLE_NAME_REQUIRED", "Role name can not blank", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSIONS_REQUIRED("ROLE_PERMISSIONS_REQUIRED", "At least one permission is required", HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_EXISTED("ROLE_ALREADY_EXISTED", "Role already existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role not found", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_SYSTEM_ROLE("CANNOT_DELETE_SYSTEM_ROLE", "Cannot delete system role", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE("ROLE_IN_USE", "Can not delete role in use", HttpStatus.BAD_REQUEST),

    // Permission
    PERMISSION_NOT_FOUND("PERMISSION_NOT_FOUND", "Permission not found", HttpStatus.BAD_REQUEST),

    // Employee
    EMPLOYEE_CODE_REQUIRED("EMPLOYEE_CODE_REQUIRED", "Employee code can not blank", HttpStatus.BAD_REQUEST),
    EMPLOYEE_FULL_NAME_REQUIRED("EMPLOYEE_FULL_NAME_REQUIRED", "Employee full name can not blank", HttpStatus.BAD_REQUEST),
    EMPLOYEE_GENDER_INVALID("EMPLOYEE_GENDER_INVALID", "Employee gender is invalid", HttpStatus.BAD_REQUEST),
    EMPLOYEE_GENDER_REQUIRED("EMPLOYEE_GENDER_REQUIRED", "Employee gender can not null", HttpStatus.BAD_REQUEST),
    EMPLOYEE_START_DATE_REQUIRED("EMPLOYEE_START_DATE_REQUIRED", "Employee start date can not null", HttpStatus.BAD_REQUEST),
    EMPLOYEE_STATUS_REQUIRED("EMPLOYEE_STATUS_REQUIRED", "Employee status can not null", HttpStatus.BAD_REQUEST),
    EMPLOYEE_STATUS_INVALID("EMPLOYEE_STATUS_INVALID", "Employee status is invalid", HttpStatus.BAD_REQUEST),
    EMPLOYEE_EMAIL_REQUIRED("EMPLOYEE_EMAIL_REQUIRED", "Employee email can not blank", HttpStatus.BAD_REQUEST),
    EMPLOYEE_EMAIL_INVALID("EMPLOYEE_EMAIL_INVALID", "Employee email is invalid", HttpStatus.BAD_REQUEST),
    EMPLOYEE_CODE_EXISTS("EMPLOYEE_CODE_EXISTS", "Employee code exists", HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_FOUND("EMPLOYEE_NOT_FOUND", "Employee not found", HttpStatus.NOT_FOUND),

    ;
    private String code;
    private String defaultMessage;
    private HttpStatus status;
}
