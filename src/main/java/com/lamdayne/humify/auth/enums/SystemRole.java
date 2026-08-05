package com.lamdayne.humify.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum SystemRole {

    SYSTEM_ADMIN("SYSTEM_ADMIN", "Full system access",
            Set.of(PermissionEnum.values())
    ),

    COMPANY_ADMIN("COMPANY_ADMIN", "Full company access",
            Set.of(
                    PermissionEnum.COMPANY_FULL,
                    PermissionEnum.EMPLOYEE_FULL,
                    PermissionEnum.USER_FULL,
                    PermissionEnum.ROLE_FULL,
                    PermissionEnum.BRANCH_FULL,
                    PermissionEnum.POSITION_FULL,
                    PermissionEnum.DEPARTMENT_FULL,
                    PermissionEnum.ATTENDANCE_FULL,
                    PermissionEnum.PERMISSION_READ,
                    PermissionEnum.PROJECT_FULL,
                    PermissionEnum.LEAVE_FULL,
                    PermissionEnum.CONTRACT_FULL,
                    PermissionEnum.PAYROLL_FULL,
                    PermissionEnum.PAYSLIP_READ,
                    PermissionEnum.PAYSLIP_UPDATE,
                    PermissionEnum.PERFORMANCE_FULL,
                    PermissionEnum.TASK_FULL,
                    PermissionEnum.ATTENDANCE_CORRECTION_FULL,
                    PermissionEnum.ATTENDANCE_LOG_READ,
                    PermissionEnum.WORK_SHIFT_FULL
            )
    ),

    HR_MANAGER("HR_MANAGER", "Manage employees and attendance",
            Set.of(
                    PermissionEnum.EMPLOYEE_FULL,
                    PermissionEnum.ATTENDANCE_FULL,
                    PermissionEnum.LEAVE_FULL,
                    PermissionEnum.WORK_SHIFT_FULL,
                    PermissionEnum.ATTENDANCE_CORRECTION_FULL,
                    PermissionEnum.ATTENDANCE_LOG_READ,
                    PermissionEnum.CONTRACT_READ,
                    PermissionEnum.CONTRACT_CREATE,
                    PermissionEnum.CONTRACT_UPDATE,
                    PermissionEnum.CONTRACT_DELETE,
                    PermissionEnum.PAYROLL_READ,
                    PermissionEnum.PAYROLL_CALCULATE,
                    PermissionEnum.PAYROLL_APPROVE,
                    PermissionEnum.PAYROLL_UPDATE,
                    PermissionEnum.PAYROLL_PERIOD_CREATE,
                    PermissionEnum.PAYSLIP_READ,
                    PermissionEnum.PAYSLIP_UPDATE,
                    PermissionEnum.KPI_READ,
                    PermissionEnum.KPI_CREATE,
                    PermissionEnum.KPI_UPDATE,
                    PermissionEnum.KPI_DELETE,
                    PermissionEnum.PERFORMANCE_REVIEW_READ,
                    PermissionEnum.PERFORMANCE_REVIEW_CREATE,
                    PermissionEnum.PERFORMANCE_REVIEW_UPDATE,
                    PermissionEnum.PERFORMANCE_REVIEW_EVALUATE,
                    PermissionEnum.PERFORMANCE_REVIEW_COMPLETE,
                    PermissionEnum.PERFORMANCE_REVIEW_MANAGE,
                    PermissionEnum.PERMISSION_READ,
                    PermissionEnum.ROLE_READ,
                    PermissionEnum.ROLE_CREATE,
                    PermissionEnum.ROLE_UPDATE,
                    PermissionEnum.USER_CREATE,
                    PermissionEnum.USER_READ,
                    PermissionEnum.USER_UPDATE,
                    PermissionEnum.BRANCH_READ,
                    PermissionEnum.DEPARTMENT_READ,
                    PermissionEnum.POSITION_READ,
                    PermissionEnum.PROJECT_READ,
                    PermissionEnum.PROJECT_UPDATE,
                    PermissionEnum.PROJECT_CREATE,
                    PermissionEnum.TASK_FULL
            )
    ),

    EMPLOYEE("EMPLOYEE", "Basic employee access",
            Set.of(
                    PermissionEnum.ATTENDANCE_READ,
                    PermissionEnum.ATTENDANCE_CHECKIN,
                    PermissionEnum.ATTENDANCE_CHECKOUT,
                    PermissionEnum.LEAVE_REQUEST_READ,
                    PermissionEnum.LEAVE_REQUEST_CREATE,
                    PermissionEnum.LEAVE_REQUEST_UPDATE,
                    PermissionEnum.LEAVE_REQUEST_CANCEL,
                    PermissionEnum.PROJECT_READ,
                    PermissionEnum.ATTENDANCE_CORRECTION_CREATE,
                    PermissionEnum.ATTENDANCE_CORRECTION_READ,
                    PermissionEnum.LEAVE_TYPE_READ,
                    PermissionEnum.PAYSLIP_READ,
                    PermissionEnum.WORK_SHIFT_READ,
                    PermissionEnum.KPI_READ,
                    PermissionEnum.PERFORMANCE_REVIEW_READ,
                    PermissionEnum.PERFORMANCE_REVIEW_SELF
            )
    );

    private final String name;
    private final String description;
    private final Set<PermissionEnum> permissions;
}
