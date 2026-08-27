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
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleServiceImpl Tests")
public class RoleServiceImplTest {

    @Mock private EntityManager em;
    @Mock private RoleRepository roleRepository;
    @Mock private PermissionRepository permissionRepository;
    @Mock private UserHasRoleRepository userHasRoleRepository;
    @Mock private RoleHasPermissionRepository roleHasPermissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private UserPrincipal userPrincipal;
    private Company company;
    private Role systemRole;
    private Role customRole;
    private Permission permission;

    @BeforeEach
    void setUp() {
        company = Company.builder().build();

        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .companyId(10L)
                .email("admin@company.com")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_CREATE"),
                        new SimpleGrantedAuthority("EMPLOYEE_READ")
                ))
                .build();

        systemRole = Role.builder()
                .name("SYSTEM_ADMIN")
                .isSystem(true)
                .build();
        systemRole.setId(1L);

        customRole = Role.builder()
                .name("HR_MANAGER")
                .company(company)
                .isSystem(false)
                .build();
        customRole.setId(2L);

        permission = Permission.builder()
                .name("EMPLOYEE_READ")
                .module("EMPLOYEE")
                .build();
        permission.setId(100L);
    }

    // ---- createRole ----

    @Test
    @DisplayName("Create role successfully")
    void createRole_success() {
        CreateRoleRequest request = mock(CreateRoleRequest.class);
        when(request.getName()).thenReturn("HR_MANAGER");
        when(request.getDescription()).thenReturn("Handles HR stuff");
        when(request.getPermissionIds()).thenReturn(List.of(100L));

        when(roleRepository.existsByCompanyIdAndName(10L, "HR_MANAGER")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(100L))).thenReturn(List.of(permission));
        when(em.getReference(Company.class, 10L)).thenReturn(company);

        RoleResponse response = roleService.createRole(userPrincipal, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("HR_MANAGER");
        verify(roleRepository).save(any(Role.class));
        verify(roleHasPermissionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Create role throws exception when role already exists")
    void createRole_throwsWhenAlreadyExists() {
        CreateRoleRequest request = mock(CreateRoleRequest.class);
        when(request.getName()).thenReturn("HR_MANAGER");

        when(roleRepository.existsByCompanyIdAndName(10L, "HR_MANAGER")).thenReturn(true);

        assertThatThrownBy(() -> roleService.createRole(userPrincipal, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_ALREADY_EXISTED));

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create role throws exception when permissions not found")
    void createRole_throwsWhenPermissionNotFound() {
        CreateRoleRequest request = mock(CreateRoleRequest.class);
        when(request.getName()).thenReturn("HR_MANAGER");
        when(request.getPermissionIds()).thenReturn(List.of(100L, 101L));

        when(roleRepository.existsByCompanyIdAndName(10L, "HR_MANAGER")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(permission)); // Mismatch: requested 2, found 1

        assertThatThrownBy(() -> roleService.createRole(userPrincipal, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PERMISSION_NOT_FOUND));
    }

    @Test
    @DisplayName("Create role throws exception when user does not have permission to assign it")
    void createRole_throwsWhenForbiddenToAssignPermission() {
        CreateRoleRequest request = mock(CreateRoleRequest.class);
        when(request.getName()).thenReturn("HR_MANAGER");
        when(request.getPermissionIds()).thenReturn(List.of(100L));

        Permission restrictedPermission = Permission.builder()
                .name("SYSTEM_SHUTDOWN")
                .module("SYSTEM")
                .build();
        restrictedPermission.setId(100L);

        when(roleRepository.existsByCompanyIdAndName(10L, "HR_MANAGER")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(100L))).thenReturn(List.of(restrictedPermission));

        // userPrincipal only has "ROLE_CREATE" and "EMPLOYEE_READ", not "SYSTEM_SHUTDOWN" or "FULL_ACCESS"
        assertThatThrownBy(() -> roleService.createRole(userPrincipal, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ---- deleteRole ----

    @Test
    @DisplayName("Delete role successfully")
    void deleteRole_success() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(customRole));
        when(userHasRoleRepository.existsByRoleId(2L)).thenReturn(false);

        roleService.deleteRole(userPrincipal, 2L);

        verify(roleRepository).delete(customRole);
    }

    @Test
    @DisplayName("Delete role throws exception when role not found")
    void deleteRole_throwsWhenNotFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deleteRole(userPrincipal, 99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    @DisplayName("Delete role throws exception when deleting a system role")
    void deleteRole_throwsWhenSystemRole() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(systemRole));

        assertThatThrownBy(() -> roleService.deleteRole(userPrincipal, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("Delete role throws exception when role is in use")
    void deleteRole_throwsWhenInUse() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(customRole));
        when(userHasRoleRepository.existsByRoleId(2L)).thenReturn(true);

        assertThatThrownBy(() -> roleService.deleteRole(userPrincipal, 2L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_IN_USE));
    }

    // ---- updateRole ----

    @Test
    @DisplayName("Update role successfully")
    void updateRole_success() {
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getName()).thenReturn("HR_SUPERVISOR");
        when(request.getDescription()).thenReturn("Handles HR team");
        when(request.getPermissionIds()).thenReturn(List.of(100L));

        when(roleRepository.findById(2L)).thenReturn(Optional.of(customRole));
        when(roleRepository.existsByCompanyIdAndName(10L, "HR_SUPERVISOR")).thenReturn(false);
        when(permissionRepository.findAllById(List.of(100L))).thenReturn(List.of(permission));

        RoleResponse response = roleService.updateRole(userPrincipal, 2L, request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("HR_SUPERVISOR");
        verify(roleHasPermissionRepository).deleteByRoleId(2L);
        verify(roleHasPermissionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Update role throws exception when role not found")
    void updateRole_throwsWhenNotFound() {
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.updateRole(userPrincipal, 99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    @DisplayName("Update role throws exception when trying to update system role")
    void updateRole_throwsWhenSystemRole() {
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(systemRole));

        assertThatThrownBy(() -> roleService.updateRole(userPrincipal, 1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ---- findAll ----

    @Test
    @DisplayName("Find all roles and filter SYSTEM_ roles")
    void findAll_success() {
        Role systemRolePrefix = Role.builder().name("SYSTEM_USER").isSystem(true).build();
        systemRolePrefix.setId(3L);

        Role globalRole = Role.builder().name("GLOBAL_AUDITOR").isSystem(true).build(); // system role but doesn't start with SYSTEM_
        globalRole.setId(4L);

        Page<Role> rolePage = new PageImpl<>(List.of(systemRolePrefix, globalRole, customRole));

        when(roleRepository.findAll(any(Pageable.class))).thenReturn(rolePage);
        when(roleHasPermissionRepository.findByRoleId(4L)).thenReturn(Collections.emptyList());
        when(roleHasPermissionRepository.findByRoleId(2L)).thenReturn(
                List.of(RoleHasPermission.builder().permission(permission).build())
        );

        PageResponse<RoleResponse> response = roleService.findAll(1, 10, "id,desc");

        assertThat(response).isNotNull();
        // systemRolePrefix should be filtered out because it starts with SYSTEM_ prefix.
        // globalRole and customRole should remain.
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getName()).isEqualTo("GLOBAL_AUDITOR");
        assertThat(response.getItems().get(1).getName()).isEqualTo("HR_MANAGER");
    }

    // ---- findById ----

    @Test
    @DisplayName("Find role by ID successfully")
    void findById_success() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(customRole));
        when(roleHasPermissionRepository.findByRoleId(2L)).thenReturn(
                List.of(RoleHasPermission.builder().permission(permission).build())
        );

        RoleResponse response = roleService.findById(2L);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("HR_MANAGER");
        assertThat(response.getPermissions()).hasSize(1);
    }

    @Test
    @DisplayName("Find role by ID throws exception when name starts with SYSTEM_")
    void findById_throwsWhenSystemPrefixed() {
        Role sysRole = Role.builder().name("SYSTEM_SUPER").isSystem(true).build();
        sysRole.setId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(sysRole));

        assertThatThrownBy(() -> roleService.findById(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    // ---- resolveRoles ----

    @Test
    @DisplayName("Resolve roles successfully")
    void resolveRoles_success() {
        List<Long> ids = List.of(2L);
        when(roleRepository.findAllById(ids)).thenReturn(List.of(customRole));

        List<Role> resolved = roleService.resolveRoles(ids);

        assertThat(resolved).hasSize(1);
        assertThat(resolved.get(0)).isEqualTo(customRole);
    }

    @Test
    @DisplayName("Resolve roles throws exception when not all roles found")
    void resolveRoles_throwsWhenRoleMissing() {
        List<Long> ids = List.of(2L, 99L);
        when(roleRepository.findAllById(ids)).thenReturn(List.of(customRole)); // Only 1 found

        assertThatThrownBy(() -> roleService.resolveRoles(ids))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    @DisplayName("Resolve roles throws exception when role name starts with SYSTEM_")
    void resolveRoles_throwsWhenSystemPrefixed() {
        Role sysRole = Role.builder().name("SYSTEM_USER").build();
        sysRole.setId(5L);

        List<Long> ids = List.of(5L);
        when(roleRepository.findAllById(ids)).thenReturn(List.of(sysRole));

        assertThatThrownBy(() -> roleService.resolveRoles(ids))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    @Test
    @DisplayName("Resolve roles throws exception when role is not system and has no company")
    void resolveRoles_throwsWhenInvalidCustomRole() {
        Role invalidRole = Role.builder().name("SOME_ROLE").isSystem(false).company(null).build();
        invalidRole.setId(6L);

        List<Long> ids = List.of(6L);
        when(roleRepository.findAllById(ids)).thenReturn(List.of(invalidRole));

        assertThatThrownBy(() -> roleService.resolveRoles(ids))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_NOT_FOUND));
    }

    // ---- assignRoles ----

    @Test
    @DisplayName("Assign roles successfully")
    void assignRoles_success() {
        User user = User.builder().company(company).build();
        user.setId(50L);

        List<Long> roleIds = List.of(2L);

        when(roleRepository.findAllById(roleIds)).thenReturn(List.of(customRole));

        roleService.assignRoles(user, roleIds);

        verify(userHasRoleRepository).deleteByUserId(50L);
        verify(userHasRoleRepository).saveAll(anyList());
    }

    // ---- findAllRoleNames ----

    @Test
    @DisplayName("Find all role names for user")
    void findAllRoleNames_success() {
        when(userHasRoleRepository.findAllRoleNameByUserId(1L)).thenReturn(List.of("HR_MANAGER", "EMPLOYEE"));

        Set<String> roleNames = roleService.findAllRoleNames(userPrincipal);

        assertThat(roleNames).containsExactlyInAnyOrder("HR_MANAGER", "EMPLOYEE");
    }

    // ---- assignCompanyAdmin & assignEmployeeRole ----

    @Test
    @DisplayName("Assign company admin role successfully")
    void assignCompanyAdmin_success() {
        User user = User.builder().company(company).build();
        user.setId(50L);

        Role adminRole = Role.builder().name(SystemRole.COMPANY_ADMIN.getName()).build();
        adminRole.setId(10L);

        when(roleRepository.findByName(SystemRole.COMPANY_ADMIN.getName())).thenReturn(Optional.of(adminRole));

        roleService.assignCompanyAdmin(user);

        ArgumentCaptor<UserHasRole> captor = ArgumentCaptor.forClass(UserHasRole.class);
        verify(userHasRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(adminRole);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Assign employee role successfully")
    void assignEmployeeRole_success() {
        User user = User.builder().company(company).build();
        user.setId(50L);

        Role empRole = Role.builder().name(SystemRole.EMPLOYEE.getName()).build();
        empRole.setId(11L);

        when(roleRepository.findByName(SystemRole.EMPLOYEE.getName())).thenReturn(Optional.of(empRole));

        roleService.assignEmployeeRole(user);

        ArgumentCaptor<UserHasRole> captor = ArgumentCaptor.forClass(UserHasRole.class);
        verify(userHasRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(empRole);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Update role throws exception when name changed to an already existing name")
    void updateRole_throwsWhenNameAlreadyExists() {
        UpdateRoleRequest request = mock(UpdateRoleRequest.class);
        when(request.getName()).thenReturn("HR_SUPERVISOR");

        when(roleRepository.findById(2L)).thenReturn(Optional.of(customRole)); // customRole name is "HR_MANAGER"
        when(roleRepository.existsByCompanyIdAndName(10L, "HR_SUPERVISOR")).thenReturn(true); // already exists

        assertThatThrownBy(() -> roleService.updateRole(userPrincipal, 2L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ROLE_ALREADY_EXISTED));
    }

    @Test
    @DisplayName("Resolve roles succeeds with system role not system-prefixed")
    void resolveRoles_successWithSystemRoleNotPrefixed() {
        Role globalRole = Role.builder().name("GLOBAL_AUDITOR").isSystem(true).company(null).build();
        globalRole.setId(4L);
        List<Long> ids = List.of(4L);

        when(roleRepository.findAllById(ids)).thenReturn(List.of(globalRole));

        List<Role> resolved = roleService.resolveRoles(ids);

        assertThat(resolved).hasSize(1);
        assertThat(resolved.get(0)).isEqualTo(globalRole);
    }
}

