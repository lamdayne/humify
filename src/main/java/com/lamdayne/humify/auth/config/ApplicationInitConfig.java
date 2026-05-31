package com.lamdayne.humify.auth.config;

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
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j(topic = "APPLICATION_INIT")
@Configuration
@RequiredArgsConstructor
public class ApplicationInitConfig {

    @Value("${system.account.email}")
    private String systemEmail;

    @Value("${system.account.password}")
    private String systemPassword;

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;

    @Bean
    public ApplicationRunner initApplicationRunner() {
        return application -> {
            initPermissions();
            initSystemRoles();
            initSystemAccount();
        };
    }

    private void initPermissions() {
        Set<String> existing = new HashSet<>(permissionRepository.findAllNames());

        List<Permission> toInsert = Arrays.stream(PermissionEnum.values())
                .filter(p -> !existing.contains(p.name()))
                .map(p -> Permission.builder()
                        .name(p.name())
                        .description(p.getDescription())
                        .module(p.getModule().name())
                        .build()
                ).toList();

        if (!toInsert.isEmpty()) {
            permissionRepository.saveAll(toInsert);
            log.info("Init {} new permissions", toInsert.size());
        }
    }

    private void initSystemRoles() {
        Map<String, Permission> permissionMap = permissionRepository.findAll()
                .stream().collect(Collectors.toMap(Permission::getName, p -> p));

        Arrays.stream(SystemRole.values()).forEach(systemRole -> {
            Role role = roleRepository.findByName(systemRole.getName())
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .name(systemRole.getName())
                                    .description(systemRole.getDescription())
                                    .company(null)
                                    .isSystem(true)
                                    .build()
                    )
            );

            syncRolePermissions(role, systemRole.getPermissions(), permissionMap);
        });
    }

    private void syncRolePermissions(
            Role role,
            Set<PermissionEnum> expected,
            Map<String, Permission> permissionMap
    ) {
        Set<String> expectedName = expected.stream().map(PermissionEnum::name).collect(Collectors.toSet());
        List<RoleHasPermission> current = roleHasPermissionRepository.findByRoleId(role.getId());

        Set<String> existing = current.stream()
                .map(rhp -> rhp.getPermission().getName())
                .collect(Collectors.toSet());

        List<RoleHasPermission> toInsert = expectedName.stream()
                .filter(name -> !existing.contains(name))
                .map(name -> RoleHasPermission.builder()
                        .role(role)
                        .permission(permissionMap.get(name))
                        .build()
                ).toList();

        if (!toInsert.isEmpty()) {
            roleHasPermissionRepository.saveAll(toInsert);
            log.info("Init {} new role permissions", toInsert.size());
        }

        List<RoleHasPermission> toDelete = current.stream()
                .filter(rhp -> !expectedName.contains(rhp.getPermission().getName()))
                .toList();

        if (!toDelete.isEmpty()) {
            roleHasPermissionRepository.deleteAll(toDelete);
            log.info("Delete {} role permissions", toDelete.size());
        }
    }

    private void initSystemAccount() {
        if (userService.existsByEmail(systemEmail)) {
            log.info("System account already exists");
            return;
        }

        Role systemAdminRole = roleRepository.findByName(SystemRole.SYSTEM_ADMIN.getName())
                .orElseThrow(() -> new IllegalStateException("SYSTEM_ADMIN role not found"));

        User systemUser = User.builder()
                .email(systemEmail)
                .password(passwordEncoder.encode(systemPassword))
                .company(null)
                .employee(null)
                .build();

        userService.save(systemUser);

        UserHasRole userHasRole = UserHasRole.builder()
                .role(systemAdminRole)
                .user(systemUser)
                .company(null)
                .build();

        userHasRoleRepository.save(userHasRole);
    }

}
