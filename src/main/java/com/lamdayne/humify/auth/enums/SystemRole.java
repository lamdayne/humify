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
                    PermissionEnum.EMPLOYEE_FULL,
                    PermissionEnum.USER_FULL,
                    PermissionEnum.ROLE_READ,
                    PermissionEnum.BRANCH_FULL,
                    PermissionEnum.POSITION_FULL,
                    PermissionEnum.DEPARTMENT_FULL,
                    PermissionEnum.ATTENDANCE_FULL,
                    PermissionEnum.PERMISSION_READ,
                    PermissionEnum.PROJECT_FULL,
                    PermissionEnum.LEAVE_REQUEST_READ,
                    PermissionEnum.LEAVE_REQUEST_APPROVE,
                    PermissionEnum.LEAVE_REQUEST_REJECT,
                    PermissionEnum.LEAVE_BALANCE_UPDATE,
                    PermissionEnum.LEAVE_BALANCE_READ,
                    PermissionEnum.CONTRACT_FULL,
                    PermissionEnum.PAYROLL_PERIOD_CREATE,
                    PermissionEnum.PAYROLL_READ,
                    PermissionEnum.PAYROLL_CALCULATE,
                    PermissionEnum.PAYROLL_APPROVE,
                    PermissionEnum.KPI_CREATE,
                    PermissionEnum.PERFORMANCE_REVIEW_MANAGE
            )
    ),

    HR_MANAGER("HR_MANAGER", "Manage employees and attendance",
            Set.of(
                    PermissionEnum.EMPLOYEE_FULL,
                    PermissionEnum.ATTENDANCE_FULL,
                    PermissionEnum.USER_CREATE,
                    PermissionEnum.USER_READ,
                    PermissionEnum.USER_UPDATE,
                    PermissionEnum.BRANCH_READ,
                    PermissionEnum.DEPARTMENT_READ,
                    PermissionEnum.POSITION_READ,
                    PermissionEnum.PROJECT_READ,
                    PermissionEnum.PROJECT_UPDATE,
                    PermissionEnum.PROJECT_CREATE,
                    PermissionEnum.LEAVE_REQUEST_READ,
                    PermissionEnum.LEAVE_REQUEST_APPROVE,
                    PermissionEnum.LEAVE_REQUEST_REJECT,
                    PermissionEnum.LEAVE_BALANCE_READ,
                    PermissionEnum.LEAVE_BALANCE_UPDATE,
                    PermissionEnum.CONTRACT_READ,
                    PermissionEnum.CONTRACT_CREATE,
                    PermissionEnum.CONTRACT_UPDATE,
                    PermissionEnum.PAYROLL_READ,
                    PermissionEnum.PAYROLL_CALCULATE,
                    PermissionEnum.KPI_CREATE
            )
    ),

    EMPLOYEE("EMPLOYEE", "Basic employee access",
            Set.of(
                    PermissionEnum.ATTENDANCE_READ,
                    PermissionEnum.ATTENDANCE_CHECKIN,
                    PermissionEnum.ATTENDANCE_CHECKOUT,
                    PermissionEnum.LEAVE_REQUEST_READ,
                    PermissionEnum.LEAVE_REQUEST_CREATE,
                    PermissionEnum.PROJECT_READ
            )
    );

    private final String name;
    private final String description;
    private final Set<PermissionEnum> permissions;
}
