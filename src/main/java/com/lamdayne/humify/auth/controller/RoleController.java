package com.lamdayne.humify.auth.controller;

import com.lamdayne.humify.auth.dto.request.CreateRoleRequest;
import com.lamdayne.humify.auth.dto.request.UpdateRoleRequest;
import com.lamdayne.humify.auth.dto.response.RoleResponse;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.service.RoleService;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_CREATE', 'ROLE_FULL')")
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
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_DELETE', 'ROLE_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "roleId") Long roleId
    ) {
        roleService.deleteRole(userPrincipal, roleId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.ROLE_DELETE_SUCCESS));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_UPDATE', 'ROLE_FULL')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "roleId") Long id,
            @RequestBody @Valid UpdateRoleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.ROLE_UPDATE_SUCCESS,
                        roleService.updateRole(userPrincipal, id, request)
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_READ', 'ROLE_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getRoles(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ROLE_READ_ALL_SUCCESS,
                        roleService.findAll(page, size, sorts)
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'ROLE_READ', 'ROLE_FULL')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(
            @PathVariable("id") Long roleId
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.ROLE_READ_SUCCESS,
                        roleService.findById(roleId)
                ));
    }

}
