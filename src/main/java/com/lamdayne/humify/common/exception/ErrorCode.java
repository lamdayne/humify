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
    ACCESS_DENIED("ACCESS_DENIED", "Access denied", HttpStatus.FORBIDDEN),

    // Branch
    COMPANY_ID_REQUIRED("COMPANY_ID_REQUIRED", "Company ID cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_REQUIRED("BRANCH_CODE_REQUIRED", "Branch code cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_INVALID("BRANCH_CODE_INVALID", "Branch code exceed max length", HttpStatus.BAD_REQUEST),
    BRANCH_NAME_REQUIRED("BRANCH_NAME_REQUIRED", "Branch name cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_FIELD_REQUIRED("BRANCH_FIELD_REQUIRED", "Branch field cannot be blank", HttpStatus.BAD_REQUEST),
    BRANCH_CODE_EXISTED("BRANCH_CODE_EXISTED", "Branch code already exists in this company", HttpStatus.BAD_REQUEST),
    BRANCH_ID_INVALID("BRANCH_ID_INVALID", "Branch ID is invalid", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_PROVIDER("UNSUPPORTED_PROVIDER", "Unsupported provider", HttpStatus.BAD_REQUEST),

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
    USER_EMAIL_EXISTED("USER_EMAIL_EXISTED", "Email already exists", HttpStatus.BAD_REQUEST),
    USER_OLD_PASSWORD_REQUIRED("USER_OLD_PASSWORD_REQUIRED", "Old password is required", HttpStatus.BAD_REQUEST),
    USER_PASSWORD_NOT_MATCH("USER_PASSWORD_NOT_MATCH", "Password does not match", HttpStatus.BAD_REQUEST),

    // Auth
    JWT_EXPIRED("JWT_EXPIRED", "Token expired", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED("UNAUTHENTICATED", "Unauthorized", HttpStatus.UNAUTHORIZED),
    EMAIL_REQUIRED("EMAIL_REQUIRED", "Email can not blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID("EMAIL_INVALID", "Email address is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED("PASSWORD_REQUIRED", "Password cannot be blank", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "Invalid refresh token", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD("INVALID_PASSWORD", "Invalid password", HttpStatus.BAD_REQUEST),
    USER_NOT_ACTIVATED("USER_NOT_ACTIVATED", "User is not activated", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", "Token not found", HttpStatus.NOT_FOUND),
    PASSWORD_NOT_MATCH("PASSWORD_NOT_MATCH", "Password does not match", HttpStatus.BAD_REQUEST),
    RESET_TOKEN_EXPIRED("RESET_TOKEN_EXPIRED", "Token expired", HttpStatus.BAD_REQUEST),
    RESET_TOKEN_USED("RESET_TOKEN_USED", "Token has been used", HttpStatus.BAD_REQUEST),
    RESET_TOKEN_REQUIRED("RESET_TOKEN_REQUIRED", "Token is required", HttpStatus.BAD_REQUEST),
    CONFIRM_PASSWORD_REQUIRED("CONFIRM_PASSWORD_REQUIRED", "Confirm password is required", HttpStatus.BAD_REQUEST),
    COMPANY_PENDING("COMPANY_PENDING", "Company is pending, please verify", HttpStatus.BAD_REQUEST),
    COMPANY_LOCKED("COMPANY_LOCKED", "Company locked", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token is expired", HttpStatus.BAD_REQUEST),
    COMPANY_ALREADY_ACTIVE("COMPANY_ALREADY_ACTIVE", "Company already active", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_EXPIRED("TOKEN_NOT_EXPIRED", "Token is not expired", HttpStatus.BAD_REQUEST),
    EXCHANGE_FAILED("EXCHANGE_FAILED", "Exchange failed", HttpStatus.BAD_REQUEST),
    ID_TOKEN_INVALID("ID_TOKEN_INVALID", "ID token is invalid", HttpStatus.BAD_REQUEST),

    // Role
    ROLE_NAME_REQUIRED("ROLE_NAME_REQUIRED", "Role name can not blank", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSIONS_REQUIRED("ROLE_PERMISSIONS_REQUIRED", "At least one permission is required", HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_EXISTED("ROLE_ALREADY_EXISTED", "Role already existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role not found", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_SYSTEM_ROLE("CANNOT_DELETE_SYSTEM_ROLE", "Cannot delete system role", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE("ROLE_IN_USE", "Can not delete role in use", HttpStatus.BAD_REQUEST),
    ROLE_ID_REQUIRED("ROLE_ID_REQUIRED", "Role ID is required", HttpStatus.BAD_REQUEST),

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
    EMPLOYEE_EMAIL_EXISTED("EMPLOYEE_EMAIL_EXISTED", "Employee email already existed", HttpStatus.BAD_REQUEST),
    EMPLOYEE_ID_DOCUMENT_NOT_FOUND("EMPLOYEE_ID_DOCUMENT_NOT_FOUND", "Employee ID document not found", HttpStatus.NOT_FOUND),

    // Media
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EMPTY("FILE_EMPTY", "File cannot be empty", HttpStatus.BAD_REQUEST),

    // Project
    PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", "Project not found", HttpStatus.NOT_FOUND),
    PROJECT_KEY_EXISTED("PROJECT_KEY_EXISTED", "Project key already exists in this company", HttpStatus.BAD_REQUEST),
    PROJECT_KEY_INVALID("PROJECT_KEY_INVALID", "Project key must be alphanumeric and 2-10 chars", HttpStatus.BAD_REQUEST),
    PROJECT_ROLE_INVALID("PROJECT_ROLE_INVALID", "Project role invalid", HttpStatus.BAD_REQUEST),
    PROJECT_ROLE_NOT_FOUND("PROJECT_ROLE_NOT_FOUND", "Project role not found", HttpStatus.NOT_FOUND),
    PROJECT_NAME_REQUIRED("PROJECT_NAME_REQUIRED", "Project name can not blank", HttpStatus.BAD_REQUEST),
    PROJECT_KEY_REQUIRED("PROJECT_KEY_REQUIRED", "Project key must be alphanumeric and 2-10 chars", HttpStatus.BAD_REQUEST),
    PROJECT_STATUS_INVALID("PROJECT_STATUS_INVALID", "Project status is invalid", HttpStatus.BAD_REQUEST),

    // Member
    MEMBER_ALREADY_EXISTS("MEMBER_ALREADY_EXISTS", "User is already a member of this project", HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "Member not found in this project", HttpStatus.NOT_FOUND),
    
    // Invitation
    INVITATION_NOT_FOUND("INVITATION_NOT_FOUND", "Invitation link is invalid or not found", HttpStatus.NOT_FOUND),
    INVITATION_EXPIRED("INVITATION_EXPIRED", "Invitation link has expired", HttpStatus.BAD_REQUEST),
    INVITATION_REVOKED("INVITATION_REVOKED", "Invitation link has been revoked", HttpStatus.BAD_REQUEST),
    INVITATION_EMAIL_MISMATCH("INVITATION_EMAIL_MISMATCH", "Authenticated email does not match invited email", HttpStatus.FORBIDDEN),
    INVITATION_TOKEN_REQUIRED("INVITATION_TOKEN_REQUIRED", "Invitation token is required", HttpStatus.BAD_REQUEST),
    
    // Sprint
    SPRINT_NOT_FOUND("SPRINT_NOT_FOUND", "Sprint not found", HttpStatus.NOT_FOUND),
    SPRINT_NAME_REQUIRED("SPRINT_NAME_REQUIRED", "Sprint name is required", HttpStatus.BAD_REQUEST),
    SPRINT_STATUS_INVALID("SPRINT_STATUS_INVALID", "Invalid sprint status transition", HttpStatus.BAD_REQUEST),
    SPRINT_DATES_INVALID("SPRINT_DATES_INVALID", "End date must be after start date", HttpStatus.BAD_REQUEST),
    SPRINT_ALREADY_ACTIVE("SPRINT_ALREADY_ACTIVE", "There is already an active sprint in this project", HttpStatus.BAD_REQUEST),
    SPRINT_ID_REQUIRED("SPRINT_ID_REQUIRED", "Sprint id is required", HttpStatus.BAD_REQUEST),

    // Column
    COLUMN_NOT_FOUND("COLUMN_NOT_FOUND", "Board column not found", HttpStatus.NOT_FOUND),
    COLUMN_POSITION_INVALID("COLUMN_POSITION_INVALID", "Column position is invalid", HttpStatus.BAD_REQUEST),
    COLUMN_REORDER_INVALID("COLUMN_REORDER_INVALID", "Column reorder request is invalid", HttpStatus.BAD_REQUEST),
    COLUMN_CATEGORY_INVALID("COLUMN_CATEGORY_INVALID", "Column category is invalid", HttpStatus.BAD_REQUEST),
    COLUMN_NAME_REQUIRED("COLUMN_NAME_REQUIRED", "Column name is required", HttpStatus.BAD_REQUEST),
    COLUMN_ID_REQUIRED("COLUMN_ID_REQUIRED", "Column id is required", HttpStatus.BAD_REQUEST),

    // Task
    TASK_NOT_FOUND("TASK_NOT_FOUND", "Task not found", HttpStatus.NOT_FOUND),
    TASK_PARENT_LOOP("TASK_PARENT_LOOP", "Task cannot be a sub-task of itself or create loops", HttpStatus.BAD_REQUEST),
    TASK_ESTIMATE_INVALID("TASK_ESTIMATE_INVALID", "Estimated hours or story points must be positive", HttpStatus.BAD_REQUEST),
    TASK_TITLE_REQUIRED("TASK_TITLE_REQUIRED", "Task title is required", HttpStatus.BAD_REQUEST),
    TASK_PRIORITY_INVALID("TASK_PRIORITY_INVALID", "Task priority is invalid", HttpStatus.BAD_REQUEST),
    TASK_TYPE_INVALID("TASK_TYPE_INVALID", "Task type is invalid", HttpStatus.BAD_REQUEST),
    TASK_POINTS_INVALID("TASK_POINTS_INVALID", "Task points is invalid", HttpStatus.BAD_REQUEST),
    TASK_ESTIMATED_HOURS_INVALID("TASK_ESTIMATED_HOURS_INVALID", "Estimated hours or story points is invalid", HttpStatus.BAD_REQUEST),
    TASK_DUE_DATE_MUST_BE_FUTURE("TASK_DUE_DATE_MUST_BE_FUTURE", "Task due date must be future", HttpStatus.BAD_REQUEST),
    INVALID_MOVE_TASK("INVALID_MOVE_TASK", "Move task is invalid", HttpStatus.BAD_REQUEST),
    TASK_ASSIGNEE_ID_REQUIRED("TASK_ASSIGNEE_ID_REQUIRED", "Assignee id is required", HttpStatus.BAD_REQUEST),
    
    // Comment
    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", "Comment not found", HttpStatus.NOT_FOUND),
    COMMENT_PARENT_NOT_FOUND("COMMENT_PARENT_NOT_FOUND", "Reply target comment not found", HttpStatus.BAD_REQUEST),
    COMMENT_CONTENT_REQUIRED("COMMENT_CONTENT_REQUIRED", "Comment content is required", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ACTION("FORBIDDEN_ACTION", "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    // Attachment
    ATTACHMENT_NOT_FOUND("ATTACHMENT_NOT_FOUND", "Attachment not found", HttpStatus.NOT_FOUND),
    
    // Worklog
    WORKLOG_NOT_FOUND("WORKLOG_NOT_FOUND", "Worklog entry not found", HttpStatus.NOT_FOUND),
    WORKLOG_HOURS_INVALID("WORKLOG_HOURS_INVALID", "Logged hours must be greater than zero", HttpStatus.BAD_REQUEST),
    
    // KPI
    KPI_NOT_FOUND("KPI_NOT_FOUND", "KPI not found", HttpStatus.NOT_FOUND),
    KPI_WEIGHT_INVALID("KPI_WEIGHT_INVALID", "Total weight of KPIs cannot exceed 1.0 (100%)", HttpStatus.BAD_REQUEST),
    
    // Performance Review
    REVIEW_NOT_FOUND("REVIEW_NOT_FOUND", "Performance review not found", HttpStatus.NOT_FOUND),
    REVIEW_PERIOD_DUPLICATE("REVIEW_PERIOD_DUPLICATE", "Review for this employee in this period already exists", HttpStatus.BAD_REQUEST),
    REVIEW_STATUS_INVALID("REVIEW_STATUS_INVALID", "Invalid performance review status transition", HttpStatus.BAD_REQUEST),
    REVIEW_CREATE_SUCCESS("REVIEW_CREATE_SUCCESS", "Performance review created successfully", HttpStatus.CREATED),
    REVIEW_READ_SUCCESS("REVIEW_READ_SUCCESS", "Performance reviews retrieved successfully", HttpStatus.OK),
    REVIEW_UPDATE_SUCCESS("REVIEW_UPDATE_SUCCESS", "Performance review updated successfully", HttpStatus.OK),
    SCORE_REQUIRED("SCORE_REQUIRED", "Score is required", HttpStatus.BAD_REQUEST),

    // Employee Certification
    EMPLOYEE_CERTIFICATION_NAME_REQUIRED("EMPLOYEE_CERTIFICATION_NAME_REQUIRED", "Employee certification name can not blank", HttpStatus.BAD_REQUEST),
    EMPLOYEE_CERTIFICATION_NOT_FOUND("EMPLOYEE_CERTIFICATION_NOT_FOUND", "Employee certification not found", HttpStatus.NOT_FOUND),

    //Employee Education
    EMPLOYEE_EDUCATION_NOT_FOUND("EMPLOYEE_EDUCATION_NOT_FOUND", "Employee education not found", HttpStatus.NOT_FOUND),
    DEGREE_LEVEL_REQUIRED("DEGREE_LEVEL_REQUIRED", "Degree level cannot be blank", HttpStatus.BAD_REQUEST),
    SCHOOL_NAME_REQUIRED("SCHOOL_NAME_REQUIRED", "School name cannot be blank", HttpStatus.BAD_REQUEST),
    START_YEAR_REQUIRED("START_YEAR_REQUIRED", "Start year cannot be null", HttpStatus.BAD_REQUEST),
    START_YEAR_AFTER_END_YEAR("START_YEAR_AFTER_END_YEAR", "Start year cannot be after end year", HttpStatus.BAD_REQUEST)

    ;

    private String code;
    private String defaultMessage;
    private HttpStatus status;
}
