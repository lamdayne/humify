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
                    PermissionEnum.ROLE_FULL,
                    PermissionEnum.BRANCH_FULL,
                    PermissionEnum.POSITION_FULL,
                    PermissionEnum.DEPARTMENT_FULL,
                    PermissionEnum.ATTENDANCE_FULL,
                    PermissionEnum.PERMISSION_READ
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
                    PermissionEnum.POSITION_READ
            )
    ),

    EMPLOYEE("EMPLOYEE", "Basic employee access",
            Set.of(
                    PermissionEnum.ATTENDANCE_READ,
                    PermissionEnum.ATTENDANCE_CHECKIN,
                    PermissionEnum.ATTENDANCE_CHECKOUT
            )
    );

    private final String name;
    private final String description;
    private final Set<PermissionEnum> permissions;
}
