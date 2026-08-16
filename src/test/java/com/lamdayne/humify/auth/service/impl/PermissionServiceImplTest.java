package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.dto.response.PermissionResponse;
import com.lamdayne.humify.auth.entity.Permission;
import com.lamdayne.humify.auth.enums.PermissionEnum;
import com.lamdayne.humify.auth.enums.PermissionModule;
import com.lamdayne.humify.auth.repository.PermissionRepository;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.PageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionServiceImpl Tests")
public class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .companyId(10L)
                .email("user@example.com")
                .authorities(List.of(
                        new SimpleGrantedAuthority("EMPLOYEE_READ"),
                        new SimpleGrantedAuthority("COMPANY_FULL")
                ))
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Get permissions successfully filters and maps items")
    void getPermissions_success() {
        Permission p1 = Permission.builder().name("EMPLOYEE_READ").description("Read employees").module("EMPLOYEE").build();
        p1.setId(1L);

        Permission p2 = Permission.builder().name(PermissionEnum.FULL_ACCESS.name()).description("Full access").module("AUTH").build();
        p2.setId(2L);

        Permission p3 = Permission.builder().name("SYSTEM_CLEANUP").description("System cleanup").module(PermissionModule.SYSTEM.name()).build();
        p3.setId(3L);

        Permission p4 = Permission.builder().name("PERMISSION_READ").description("Read permissions").module(PermissionModule.PERMISSION.name()).build();
        p4.setId(4L);

        Permission p5 = Permission.builder().name("EMPLOYEE_FULL").description("Employee full control").module("EMPLOYEE").build();
        p5.setId(5L);

        Permission p6 = Permission.builder().name("COMPANY_UPDATE").description("Update company").module("COMPANY").build();
        p6.setId(6L);

        List<Permission> allPermissions = List.of(p1, p2, p3, p4, p5, p6);
        Page<Permission> permissionPage = new PageImpl<>(allPermissions);

        when(permissionRepository.findAll(any(Pageable.class))).thenReturn(permissionPage);

        PageResponse<PermissionResponse> response = permissionService.getPermissions(1, 10, "id,asc");

        assertThat(response).isNotNull();
        assertThat(response.getPageNo()).isEqualTo(1);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(6);

        // Expected output after filtering:
        // p1 (EMPLOYEE_READ) - included. Can assign? user has EMPLOYEE_READ -> true
        // p2 (FULL_ACCESS) - excluded
        // p3 (SYSTEM_CLEANUP) - excluded (module is SYSTEM)
        // p4 (PERMISSION_READ) - excluded (module is PERMISSION)
        // p5 (EMPLOYEE_FULL) - excluded (ends with _FULL)
        // p6 (COMPANY_UPDATE) - included. Can assign? user has COMPANY_FULL -> true
        List<PermissionResponse> items = response.getItems();
        assertThat(items).hasSize(2);

        PermissionResponse response1 = items.get(0);
        assertThat(response1.getId()).isEqualTo(1L);
        assertThat(response1.getName()).isEqualTo("EMPLOYEE_READ");
        assertThat(response1.getCanAssign()).isTrue();

        PermissionResponse response2 = items.get(1);
        assertThat(response2.getId()).isEqualTo(6L);
        assertThat(response2.getName()).isEqualTo("COMPANY_UPDATE");
        assertThat(response2.getCanAssign()).isTrue();
    }
}
