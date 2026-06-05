package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.dto.request.CreateRoleRequest;
import com.lamdayne.humify.auth.dto.request.UpdateRoleRequest;
import com.lamdayne.humify.auth.dto.response.RoleResponse;
import com.lamdayne.humify.auth.entity.Permission;
import com.lamdayne.humify.auth.entity.Role;
import com.lamdayne.humify.auth.entity.RoleHasPermission;
import com.lamdayne.humify.auth.enums.PermissionEnum;
import com.lamdayne.humify.auth.enums.PermissionModule;
import com.lamdayne.humify.auth.repository.PermissionRepository;
import com.lamdayne.humify.auth.repository.RoleHasPermissionRepository;
import com.lamdayne.humify.auth.repository.RoleRepository;
import com.lamdayne.humify.auth.repository.UserHasRoleRepository;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.service.RoleService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final EntityManager em;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;

    @Override
    @Transactional
    public RoleResponse createRole(UserPrincipal userPrincipal, CreateRoleRequest request) {
        if (roleRepository.existsByCompanyIdAndName(userPrincipal.getCompanyId(), request.getName())) {
            throw new AppException(ErrorCode.ROLE_ALREADY_EXISTED);
        }

        List<Long> permissionIds = request.getPermissionIds().stream().distinct().toList();

        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissionIds.size() != permissions.size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }

        boolean isSysAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals(PermissionEnum.FULL_ACCESS.name())
                );

        validatePermissions(permissions, isSysAdmin);

        Role role = Role.builder()
                .company(em.getReference(Company.class, userPrincipal.getCompanyId()))
                .name(request.getName())
                .description(request.getDescription())
                .build();

        roleRepository.save(role);

        List<RoleHasPermission> roleHasPermissions = permissions.stream()
                .map(permission -> RoleHasPermission.builder()
                        .role(role)
                        .permission(permission)
                        .build()
                ).toList();

        roleHasPermissionRepository.saveAll(roleHasPermissions);

        log.info("Created role: {} for company: {}", role.getName(), userPrincipal.getCompanyId());

        return RoleResponse.from(role, permissions);
    }

    @Override
    @Transactional
    public void deleteRole(UserPrincipal userPrincipal, Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new AppException(ErrorCode.ROLE_ALREADY_EXISTED);
        }

        if (userHasRoleRepository.existsByRoleId(roleId)) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }

        roleRepository.delete(role);
        log.info("Deleted role: {} for company: {}", role.getName(), userPrincipal.getCompanyId());
    }

    @Override
    public RoleResponse updateRole(UserPrincipal userPrincipal, Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        // update permission
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());

        validatePermissions(permissions, role.getIsSystem());

        // clear old
        roleHasPermissionRepository.deleteByRoleId(roleId);

        // insert new
        List<RoleHasPermission> roleHasPermissions = permissions.stream()
                .map(p -> RoleHasPermission.builder()
                        .role(role)
                        .permission(p)
                        .build()
                ).toList();

        roleHasPermissionRepository.saveAll(roleHasPermissions);

        return RoleResponse.from(role, permissions);
    }

    private void validatePermissions(List<Permission> permissions, boolean isSysAdmin) {
        for (Permission permission : permissions) {
            String name = permission.getName();

            if (!isSysAdmin) {
                if (name.equals("FULL_ACCESS")) {
                    throw new AppException(ErrorCode.FORBIDDEN);
                }

                if (name.endsWith("_FULL")) {
                    throw new AppException(ErrorCode.FORBIDDEN);
                }

                if (Objects.equals(permission.getModule(), PermissionModule.SYSTEM.name())) {
                    throw new AppException(ErrorCode.FORBIDDEN);
                }
            }
        }
    }
}
