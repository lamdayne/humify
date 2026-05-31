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

    // Branch
    COMPANY_ID_REQUIRED("COMPANY_ID_REQUIRED", "Company ID cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_REQUIRED("BRANCH_CODE_REQUIRED", "Branch code cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_INVALID("BRANCH_CODE_INVALID", "Branch code exceed max length", HttpStatus.BAD_REQUEST),
    BRANCH_NAME_REQUIRED("BRANCH_NAME_REQUIRED", "Branch name cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_FIELD_REQUIRED("BRANCH_FIELD_REQUIRED", "Branch field cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_EXISTED("BRANCH_CODE_EXISTED", "Branch code already exists in this company", HttpStatus.BAD_REQUEST),

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
    BRANCH_ID_REQUIRED("BRANCH_ID_REQUIRED", "branch id can not blank", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NAME_REQUIRED("BRANCH_NOT_FOUND", "Department name can not blank", HttpStatus.BAD_REQUEST),

    // User
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.BAD_REQUEST),

    // Auth
    JWT_EXPIRED("JWT_EXPIRED", "JWT expired", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthorized", HttpStatus.UNAUTHORIZED),
    EMAIL_REQUIRED("EMAIL_REQUIRED", "Email can not blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID("EMAIL_INVALID", "Email address is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("PASSWORD_REQUIRED", "Password cannot be blank", HttpStatus.BAD_REQUEST),

    // Role
    ROLE_NAME_REQUIRED("ROLE_NAME_REQUIRED", "Role name can not blank", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSIONS_REQUIRED("ROLE_PERMISSIONS_REQUIRED", "At least one permission is required", HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_EXISTED("ROLE_ALREADY_EXISTED", "Role already existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role not found", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_SYSTEM_ROLE("CANNOT_DELETE_SYSTEM_ROLE", "Cannot delete system role", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE("ROLE_IN_USE", "Can not delete role in use", HttpStatus.BAD_REQUEST),

    // Permission
    PERMISSION_NOT_FOUND("PERMISSION_NOT_FOUND", "Permission not found", HttpStatus.BAD_REQUEST),

    ;
    private String code;
    private String defaultMessage;
    private HttpStatus status;
}
