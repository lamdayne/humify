package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.dto.request.CreateRoleRequest;
import com.lamdayne.humify.auth.dto.request.UpdateRoleRequest;
import com.lamdayne.humify.auth.dto.response.RoleResponse;
import com.lamdayne.humify.auth.entity.Permission;
import com.lamdayne.humify.auth.entity.Role;
import com.lamdayne.humify.auth.entity.RoleHasPermission;
import com.lamdayne.humify.auth.entity.UserHasRole;
import com.lamdayne.humify.auth.enums.PermissionEnum;
import com.lamdayne.humify.auth.enums.SystemRole;
import com.lamdayne.humify.auth.repository.PermissionRepository;
import com.lamdayne.humify.auth.repository.RoleHasPermissionRepository;
import com.lamdayne.humify.auth.repository.RoleRepository;
import com.lamdayne.humify.auth.repository.UserHasRoleRepository;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.auth.service.RoleService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService, RoleAccessService {

    private static final String SYSTEM_ROLE_PREFIX = "SYSTEM_";

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

        List<Permission> permissions = resolvePermissions(userPrincipal, request.getPermissionIds());

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
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (userHasRoleRepository.existsByRoleId(roleId)) {
            throw new AppException(ErrorCode.ROLE_IN_USE);
        }

        roleRepository.delete(role);
        log.info("Deleted role: {} for company: {}", role.getName(), userPrincipal.getCompanyId());
    }

    @Override
    @Transactional
    public RoleResponse updateRole(UserPrincipal userPrincipal, Long roleId, UpdateRoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (!role.getName().equals(request.getName())
                && roleRepository.existsByCompanyIdAndName(userPrincipal.getCompanyId(), request.getName())
        ) {
            throw new AppException(ErrorCode.ROLE_ALREADY_EXISTED);
        }

        String oldName = role.getName();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        List<Permission> permissions = resolvePermissions(userPrincipal, request.getPermissionIds());

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

        log.info("Updated role: [{}] -> [{}] for company: {}", oldName, role.getName(), userPrincipal.getCompanyId());

        return RoleResponse.from(role, permissions);
    }

    @Override
    public PageResponse<RoleResponse> findAll(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Role> roles = roleRepository.findAll(pageable);

        List<Role> filteredRoles = roles.stream()
                .filter(role -> {
                    if (role.getName().startsWith(SYSTEM_ROLE_PREFIX)) {
                        return false;
                    }
                    return role.getIsSystem() || role.getCompany() != null;
                })
                .toList();

        List<RoleResponse> roleResponses = filteredRoles.stream()
                .map(role -> {
                    List<Permission> permissions = roleHasPermissionRepository.findByRoleId(role.getId())
                            .stream()
                            .map(RoleHasPermission::getPermission)
                            .toList();
                    return RoleResponse.from(role, permissions);
                })
                .toList();

        return PageResponse.<RoleResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(roles.getTotalElements())
                .totalPages(roles.getTotalPages())
                .items(roleResponses)
                .build();
    }

    @Override
    public RoleResponse findById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        if (role.getName().startsWith(SYSTEM_ROLE_PREFIX)) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        List<Permission> permissions = roleHasPermissionRepository.findByRoleId(role.getId())
                .stream()
                .map(RoleHasPermission::getPermission)
                .toList();

        return RoleResponse.from(role, permissions);
    }

    private void validatePermissions(UserPrincipal user, List<Permission> permissions) {
        Set<String> userPermission = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean isSysAdmin = userPermission.contains(PermissionEnum.FULL_ACCESS.name());

        for (Permission permission : permissions) {
            if (!isAllowed(userPermission, isSysAdmin, permission.getName())) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        }
    }

    private boolean isAllowed(Set<String> userPermissions, boolean isSysAdmin, String permissionName) {
        if (permissionName.startsWith(SYSTEM_ROLE_PREFIX)) {
            return isSysAdmin;
        }

        if (userPermissions.contains(permissionName)) {
            return true;
        }

        String module = permissionName.contains("_") ? permissionName.split("_")[0] : permissionName;
        return userPermissions.contains(String.format("%s_FULL", module));
    }

    private List<Permission> resolvePermissions(UserPrincipal userPrincipal, List<Long> rawIds) {
        List<Long> permissionIds = rawIds.stream().distinct().toList();
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);

        if (permissionIds.size() != permissions.size()) {
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }

        validatePermissions(userPrincipal, permissions);
        return permissions;
    }

    @Override
    public List<Role> resolveRoles(List<Long> rawIds) {
        List<Long> roleIds = rawIds.stream().distinct().toList();
        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roleIds.size() != roles.size()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        for (Role role : roles) {
            if (role.getName().startsWith(SYSTEM_ROLE_PREFIX)) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }

            if (Boolean.FALSE.equals(role.getIsSystem()) && role.getCompany() == null) {
                throw new AppException(ErrorCode.ROLE_NOT_FOUND);
            }
        }

        return roles;
    }

    @Override
    @Transactional
    public void assignRoles(User user, List<Long> roleIds) {
        userHasRoleRepository.deleteByUserId(user.getId());

        List<Role> roles = resolveRoles(roleIds);

        List<UserHasRole> userHasRoles = roles.stream()
                .map(role -> UserHasRole.builder()
                        .company(user.getCompany())
                        .role(role)
                        .user(user)
                        .build()
                ).toList();

        userHasRoleRepository.saveAll(userHasRoles);
    }

    @Override
    public Set<String> findAllRoleNames(UserPrincipal userPrincipal) {
        return new HashSet<>(userHasRoleRepository.findAllRoleNameByUserId(userPrincipal.getId()));
    }

    @Override
    @Transactional
    public void assignCompanyAdmin(User user) {
        Role role = roleRepository.findByName(SystemRole.COMPANY_ADMIN.getName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        UserHasRole userHasRole = UserHasRole.builder()
                .role(role)
                .user(user)
                .company(user.getCompany())
                .build();

        userHasRoleRepository.save(userHasRole);
    }

    @Override
    @Transactional
    public void assignEmployeeRole(User user) {
        Role role = roleRepository.findByName(SystemRole.EMPLOYEE.getName())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        UserHasRole userHasRole = UserHasRole.builder()
                .role(role)
                .user(user)
                .company(user.getCompany())
                .build();

        userHasRoleRepository.save(userHasRole);
    }

}
