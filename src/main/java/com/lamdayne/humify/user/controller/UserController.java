package com.lamdayne.humify.user.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
