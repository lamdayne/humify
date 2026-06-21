package com.lamdayne.humify.auth.controller;

import com.lamdayne.humify.auth.dto.request.ForgotPasswordRequest;
import com.lamdayne.humify.auth.dto.request.ResetPasswordRequest;
import com.lamdayne.humify.auth.dto.request.SignInRequest;
import com.lamdayne.humify.auth.dto.response.TokenResponse;
import com.lamdayne.humify.auth.dto.response.UserMeResponse;
import com.lamdayne.humify.auth.security.auth.AuthenticationService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid SignInRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.LOGIN_SUCCESS, authenticationService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.REFRESH_TOKEN_SUCCESS, authenticationService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(SuccessCode.LOGOUT_SUCCESS));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgot(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            CompanyContext.setAdmin(true);
            authenticationService.forgot(request);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.FORGOT_PASSWORD_SUCCESS));
        } finally {
            CompanyContext.clear();
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> reset(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            CompanyContext.setAdmin(true);
            authenticationService.resetPassword(request);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.RESET_PASSWORD_SUCCESS));
        } finally {
            CompanyContext.clear();
        }
    }

    @PostMapping("/verify-company")
    public ResponseEntity<ApiResponse<Void>> verifyCompany(HttpServletRequest request) {
        try {
            CompanyContext.setAdmin(true);
            authenticationService.verifyCompany(request);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(SuccessCode.VERIFY_COMPANY_SUCCESS));
        } finally {
            CompanyContext.clear();
        }
    }

    @PostMapping("/resend-verify")
    public ResponseEntity<ApiResponse<Void>> resendVerification(HttpServletRequest request) {
        try {
            CompanyContext.setAdmin(true);
            authenticationService.resendVerifyCompany(request);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(SuccessCode.RESEND_EMAIL_SUCCESS));
        } finally {
            CompanyContext.clear();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> me(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.GET_MY_INFO_SUCCESS,
                        authenticationService.me(user)
                ));
    }

}
