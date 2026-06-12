package com.lamdayne.humify.auth.validator;

import com.lamdayne.humify.auth.entity.Permission;
import com.lamdayne.humify.auth.enums.PermissionEnum;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public class RoleValidator {

    private RoleValidator() {
        /* This utility class should not be instantiated */
    }

    public static boolean canAssign(UserPrincipal user, Permission permission) {
        String permissionName = permission.getName();

        Set<String> userPermissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean isSysAdmin = userPermissions.contains(PermissionEnum.FULL_ACCESS.name());

        if (permissionName.startsWith("SYSTEM_") && !isSysAdmin) {
            return false;
        }

        if (userPermissions.contains(permissionName)) {
            return true;
        }

        String module = permissionName.split("_")[0];
        return userPermissions.contains(String.format("%s_FULL", module));
    }

}
