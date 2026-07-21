package com.lamdayne.humify.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionEnum {

    // Employee
    EMPLOYEE_READ("View employee data", PermissionModule.EMPLOYEE),
    EMPLOYEE_CREATE("Create new employee", PermissionModule.EMPLOYEE),
    EMPLOYEE_UPDATE("Update employee information", PermissionModule.EMPLOYEE),
    EMPLOYEE_DELETE("Delete employee", PermissionModule.EMPLOYEE),
    EMPLOYEE_FULL("Full access resources employee", PermissionModule.EMPLOYEE),

    // User
    USER_READ("View user data", PermissionModule.USER),
    USER_CREATE("Create new user", PermissionModule.USER),
    USER_UPDATE("Update user information", PermissionModule.USER),
    USER_DELETE("Delete user", PermissionModule.USER),
    USER_FULL("Full access resources user", PermissionModule.USER),

    // Company
    COMPANY_READ("View company data", PermissionModule.COMPANY),
    COMPANY_CREATE("Create new company", PermissionModule.COMPANY),
    COMPANY_UPDATE("Update company information", PermissionModule.COMPANY),
    COMPANY_DELETE("Delete company", PermissionModule.COMPANY),
    COMPANY_FULL("Full access resources company", PermissionModule.COMPANY),

    // Branch
    BRANCH_READ("View branch data", PermissionModule.BRANCH),
    BRANCH_CREATE("Create new branch", PermissionModule.BRANCH),
    BRANCH_UPDATE("Update branch information", PermissionModule.BRANCH),
    BRANCH_DELETE("Delete branch", PermissionModule.BRANCH),
    BRANCH_FULL("Full access resources branch", PermissionModule.BRANCH),

    // Department
    DEPARTMENT_READ("View department data", PermissionModule.DEPARTMENT),
    DEPARTMENT_CREATE("Create new department", PermissionModule.DEPARTMENT),
    DEPARTMENT_UPDATE("Update department information", PermissionModule.DEPARTMENT),
    DEPARTMENT_DELETE("Delete department", PermissionModule.DEPARTMENT),
    DEPARTMENT_FULL("Full access resources department", PermissionModule.DEPARTMENT),

    // Position
    POSITION_READ("View position data", PermissionModule.POSITION),
    POSITION_CREATE("Create new position", PermissionModule.POSITION),
    POSITION_UPDATE("Update position information", PermissionModule.POSITION),
    POSITION_DELETE("Delete position", PermissionModule.POSITION),
    POSITION_FULL("Full access resources position", PermissionModule.POSITION),

    // Attendance
    ATTENDANCE_READ("View attendance data", PermissionModule.ATTENDANCE),
    ATTENDANCE_CHECKIN("Checkin", PermissionModule.ATTENDANCE),
    ATTENDANCE_CHECKOUT("Checkout", PermissionModule.ATTENDANCE),
    ATTENDANCE_UPDATE("Update attendance record", PermissionModule.ATTENDANCE),
    ATTENDANCE_FULL("Full access resources attendance", PermissionModule.ATTENDANCE),

    // Role
    ROLE_READ("View role data", PermissionModule.ROLE),
    ROLE_CREATE("Create new role", PermissionModule.ROLE),
    ROLE_UPDATE("Update role information", PermissionModule.ROLE),
    ROLE_DELETE("Delete role", PermissionModule.ROLE),
    ROLE_FULL("Full access role", PermissionModule.ROLE),

    // Permission
    PERMISSION_READ("View permission data", PermissionModule.PERMISSION),
    PERMISSION_CREATE("Create new permission", PermissionModule.PERMISSION),
    PERMISSION_UPDATE("Update permission information", PermissionModule.PERMISSION),
    PERMISSION_DELETE("Delete permission", PermissionModule.PERMISSION),
    PERMISSION_FULL("Full access permission", PermissionModule.PERMISSION),

    // Full Access
    FULL_ACCESS("Just for SYSTEM_ADMIN", PermissionModule.SYSTEM),

    // Project
    PROJECT_READ("View project data", PermissionModule.PROJECT),
    PROJECT_CREATE("Create new project", PermissionModule.PROJECT),
    PROJECT_UPDATE("Update project information", PermissionModule.PROJECT),
    PROJECT_DELETE("Delete project", PermissionModule.PROJECT),
    PROJECT_FULL("Full access project", PermissionModule.PROJECT),

    // Leave
    LEAVE_REQUEST_READ("View leave request data", PermissionModule.LEAVE),
    LEAVE_REQUEST_CREATE("Create new leave request", PermissionModule.LEAVE),
    LEAVE_REQUEST_UPDATE("Update leave request information", PermissionModule.LEAVE),
    LEAVE_REQUEST_CANCEL("Cancel leave request", PermissionModule.LEAVE),
    LEAVE_REQUEST_APPROVE("Approve leave request", PermissionModule.LEAVE),
    LEAVE_REQUEST_REJECT("Reject leave request", PermissionModule.LEAVE),
    LEAVE_REQUEST_READ_ALL("Read all leave requests", PermissionModule.LEAVE),
    LEAVE_BALANCE_READ("View leave balance data", PermissionModule.LEAVE),
    LEAVE_BALANCE_UPDATE("Update leave balance information", PermissionModule.LEAVE),
    LEAVE_TYPE_READ("View leave type data", PermissionModule.LEAVE),
    LEAVE_TYPE_CREATE("Create new leave type information", PermissionModule.LEAVE),
    LEAVE_TYPE_UPDATE("Update leave type information", PermissionModule.LEAVE),

    // Contract
    CONTRACT_READ("View contract data", PermissionModule.CONTRACT),
    CONTRACT_CREATE("Create new contract information", PermissionModule.CONTRACT),
    CONTRACT_UPDATE("Update contract information", PermissionModule.CONTRACT),
    CONTRACT_DELETE("Delete contract", PermissionModule.CONTRACT),
    CONTRACT_FULL("Full contract information", PermissionModule.CONTRACT),

    // Payroll
    PAYROLL_PERIOD_CREATE("Create payroll periods", PermissionModule.PAYROLL),
    PAYROLL_READ("View payroll data", PermissionModule.PAYROLL),
    PAYROLL_CALCULATE("Calculate payroll", PermissionModule.PAYROLL),
    PAYROLL_APPROVE("Approve payroll", PermissionModule.PAYROLL),

    // Kpi
    KPI_CREATE("Create KPI targets", PermissionModule.PERFORMANCE),
    KPI_UPDATE("Update KPI information", PermissionModule.PERFORMANCE),
    PERFORMANCE_REVIEW_MANAGE("Manage review cycles", PermissionModule.PERFORMANCE),

    // Task
    TASK_CREATE("Create task information", PermissionModule.TASK),
    TASK_UPDATE("Update task information", PermissionModule.TASK),
    TASK_DELETE("Delete task", PermissionModule.TASK),
    TASK_READ("View task information", PermissionModule.TASK),
    TASK_ATTACHMENT("Attachment file", PermissionModule.TASK),
    TASK_COMMENT("Comment on task", PermissionModule.TASK),
    TASK_FULL("Full task information", PermissionModule.TASK),

    // Attendance Correction
    ATTENDANCE_CORRECTION_APPROVE("Correction approve", PermissionModule.ATTENDANCE),
    ATTENDANCE_CORRECTION_REJECT("Correction reject", PermissionModule.ATTENDANCE),
    ATTENDANCE_CORRECTION_READ("Correction read", PermissionModule.ATTENDANCE),
    ATTENDANCE_CORRECTION_FULL("Correction full", PermissionModule.ATTENDANCE),
    ATTENDANCE_CORRECTION_CREATE("Correction create", PermissionModule.ATTENDANCE),

    // Attendance Log
    ATTENDANCE_LOG_READ("Attendance log information", PermissionModule.ATTENDANCE),

    ;

    private final String description;
    private final PermissionModule module;
}
