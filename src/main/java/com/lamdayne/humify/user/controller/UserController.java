package com.lamdayne.humify.user.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.user.dto.request.ChangePasswordRequest;
import com.lamdayne.humify.user.dto.request.ChangeRoleRequest;
import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.service.UserService;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'USER_CREATE', 'USER_FULL')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.USER_CREATE_SUCCESS,
                        userService.create(request)
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'USER_READ', 'USER_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findAll(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.USER_READ_SUCCESS,
                        userService.findAll(page, size, sorts)
                ));
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'USER_UPDATE', 'USER_FULL')")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid ChangeRoleRequest request
    ) {
        userService.changeRole(userId, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.USER_CHANGE_ROLE_SUCCESS));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'USER_READ', 'USER_FULL')")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.USER_READ_SUCCESS,
                        userService.findById(userId)
                ));
    }

    @PutMapping("/{userId}/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> changePassword(
            @PathVariable("userId") Long userId,
            @RequestBody @Valid ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userService.changePassword(userId, request, userPrincipal);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.USER_CHANGE_PASSWORD_SUCCESS));
    }

}
