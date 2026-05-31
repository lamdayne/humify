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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
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

}
