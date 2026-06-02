package com.lamdayne.humify.auth.controller;

import com.lamdayne.humify.auth.dto.request.CreateRoleRequest;
import com.lamdayne.humify.auth.dto.response.RoleResponse;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.service.RoleService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid CreateRoleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.ROLE_CREATE_SUCCESS,
                        roleService.createRole(userPrincipal, request)
                ));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "roleId") Long roleId
    ) {
        roleService.deleteRole(userPrincipal, roleId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.ROLE_DELETE_SUCCESS));
    }

}
