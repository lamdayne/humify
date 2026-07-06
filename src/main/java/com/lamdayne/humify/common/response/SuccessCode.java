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
    GET_LOGIN_URL_SUCCESS("GET_LOGIN_URL_SUCCESS", "Get Login Url Success"),

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
    COMPANY_DELETE_SUCCESS("COMPANY_DELETE_SUCCESS", "Company deleted successfully"),

    // Position
    POSITION_CREATE_SUCCESS("POSITION_CREATE_SUCCESS", "Position created successfully"),
    FOUND_POSITION_SUCCESS("FOUND_POSITION_SUCCESS", "Get positions data successfully"),
    POSITION_UPDATE_SUCCESS("POSITION_UPDATE_SUCCESS", "Position updated successfully"),
    POSITION_DELETE_SUCCESS("POSITION_DELETE_SUCCESS", "Position deleted successfully"),

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
    USER_CHANGE_PASSWORD_SUCCESS("USER_CHANGE_PASSWORD_SUCCESS", "Change password successfully"),

    // Media
    FILE_UPLOAD_SUCCESS("FILE_UPLOAD_SUCCESS", "File uploaded successfully"),

    // Project
    PROJECT_CREATE_SUCCESS("PROJECT_CREATE_SUCCESS", "Project created successfully"),
    PROJECT_READ_SUCCESS("PROJECT_READ_SUCCESS", "Get project data successfully"),
    PROJECT_UPDATE_SUCCESS("PROJECT_UPDATE_SUCCESS", "Project updated successfully"),
    PROJECT_DELETE_SUCCESS("PROJECT_DELETE_SUCCESS", "Project deleted successfully"),
    
    // Member
    MEMBER_ADD_SUCCESS("MEMBER_ADD_SUCCESS", "Member added to project successfully"),
    MEMBER_READ_SUCCESS("MEMBER_READ_SUCCESS", "Get members successfully"),
    MEMBER_UPDATE_ROLE_SUCCESS("MEMBER_UPDATE_ROLE_SUCCESS", "Member role updated successfully"),
    MEMBER_REMOVE_SUCCESS("MEMBER_REMOVE_SUCCESS", "Member removed from project successfully"),
    MEMBER_APPROVE_SUCCESS("MEMBER_APPROVE_SUCCESS", "Member approved successfully"),
    MEMBER_JOIN_SUCCESS("MEMBER_JOIN_SUCCESS", "Member joined successfully"),
    
    // Invitation
    INVITATION_CREATE_SUCCESS("INVITATION_CREATE_SUCCESS", "Invitation link generated successfully"),
    INVITATION_VALIDATE_SUCCESS("INVITATION_VALIDATE_SUCCESS", "Invitation token is valid"),
    INVITATION_ACCEPT_SUCCESS("INVITATION_ACCEPT_SUCCESS", "Invitation accepted successfully"),
    
    // Sprint
    SPRINT_CREATE_SUCCESS("SPRINT_CREATE_SUCCESS", "Sprint created successfully"),
    SPRINT_READ_SUCCESS("SPRINT_READ_SUCCESS", "Get sprint data successfully"),
    SPRINT_UPDATE_SUCCESS("SPRINT_UPDATE_SUCCESS", "Sprint updated successfully"),
    SPRINT_DELETE_SUCCESS("SPRINT_DELETE_SUCCESS", "Sprint deleted successfully"),
    SPRINT_STATUS_UPDATE_SUCCESS("SPRINT_STATUS_UPDATE_SUCCESS", "Sprint status updated successfully"),
    
    // Column
    COLUMN_CREATE_SUCCESS("COLUMN_CREATE_SUCCESS", "Column created successfully"),
    COLUMN_READ_SUCCESS("COLUMN_READ_SUCCESS", "Get columns successfully"),
    COLUMN_UPDATE_SUCCESS("COLUMN_UPDATE_SUCCESS", "Column updated successfully"),
    COLUMN_DELETE_SUCCESS("COLUMN_DELETE_SUCCESS", "Column deleted successfully"),
    COLUMN_REORDER_SUCCESS("COLUMN_REORDER_SUCCESS", "Columns reordered successfully"),
    
    // Task
    TASK_CREATE_SUCCESS("TASK_CREATE_SUCCESS", "Task created successfully"),
    TASK_READ_SUCCESS("TASK_READ_SUCCESS", "Get task data successfully"),
    TASK_UPDATE_SUCCESS("TASK_UPDATE_SUCCESS", "Task updated successfully"),
    TASK_DELETE_SUCCESS("TASK_DELETE_SUCCESS", "Task deleted successfully"),
    TASK_MOVE_SUCCESS("TASK_MOVE_SUCCESS", "Task moved successfully"),
    TASK_ASSIGN_SUCCESS("TASK_ASSIGN_SUCCESS", "Task assigned successfully"),
    TASK_REORDER_SUCCESS("TASK_REORDER_SUCCESS", "Task reordered successfully"),
    
    // Comment
    COMMENT_CREATE_SUCCESS("COMMENT_CREATE_SUCCESS", "Comment posted successfully"),
    COMMENT_READ_SUCCESS("COMMENT_READ_SUCCESS", "Get comments successfully"),
    COMMENT_UPDATE_SUCCESS("COMMENT_UPDATE_SUCCESS", "Comment updated successfully"),
    COMMENT_DELETE_SUCCESS("COMMENT_DELETE_SUCCESS", "Comment deleted successfully"),
    
    // Attachment
    ATTACHMENT_UPLOAD_SUCCESS("ATTACHMENT_UPLOAD_SUCCESS", "Attachment uploaded successfully"),
    ATTACHMENT_DELETE_SUCCESS("ATTACHMENT_DELETE_SUCCESS", "Attachment deleted successfully"),
    
    // Worklog
    WORKLOG_CREATE_SUCCESS("WORKLOG_CREATE_SUCCESS", "Worklog added successfully"),
    WORKLOG_READ_SUCCESS("WORKLOG_READ_SUCCESS", "Get worklogs successfully"),
    WORKLOG_UPDATE_SUCCESS("WORKLOG_UPDATE_SUCCESS", "Worklog updated successfully"),
    WORKLOG_DELETE_SUCCESS("WORKLOG_DELETE_SUCCESS", "Worklog deleted successfully"),
    
    // KPI
    KPI_CREATE_SUCCESS("KPI_CREATE_SUCCESS", "KPI target created successfully"),
    KPI_READ_SUCCESS("KPI_READ_SUCCESS", "Get KPI data successfully"),
    KPI_UPDATE_SUCCESS("KPI_UPDATE_SUCCESS", "KPI updated successfully"),
    KPI_DELETE_SUCCESS("KPI_DELETE_SUCCESS", "KPI deleted successfully"),
    KPI_PROGRESS_UPDATE_SUCCESS("KPI_PROGRESS_UPDATE_SUCCESS", "KPI progress updated successfully"),
    
    // Performance Review
    REVIEW_CREATE_SUCCESS("REVIEW_CREATE_SUCCESS", "Performance review created successfully"),
    REVIEW_READ_SUCCESS("REVIEW_READ_SUCCESS", "Get review data successfully"),
    REVIEW_UPDATE_SUCCESS("REVIEW_UPDATE_SUCCESS", "Review updated successfully"),
    REVIEW_SUBMIT_SUCCESS("REVIEW_SUBMIT_SUCCESS", "Review submitted successfully"),
    REVIEW_COMPLETE_SUCCESS("REVIEW_COMPLETE_SUCCESS", "Review finalized successfully"),

    // Employee Certification
    EMPLOYEE_CERTIFICATION_CREATE_SUCCESS("EMPLOYEE_CERTIFICATION_CREATE_SUCCESS", "Employee certification added successfully"),
    EMPLOYEE_CERTIFICATION_UPDATE_SUCCESS("EMPLOYEE_CERTIFICATION_UPDATE_SUCCESS", "Employee certification updated successfully"),
    EMPLOYEE_CERTIFICATION_DELETE_SUCCESS("EMPLOYEE_CERTIFICATION_DELETE_SUCCESS", "Employee certification deleted successfully"),
    EMPLOYEE_CERTIFICATION_READ_SUCCESS("EMPLOYEE_CERTIFICATION_READ_SUCCESS", "Employee certification read successfully"),

    ;

    private String code;
    private String defaultMessage;
}
