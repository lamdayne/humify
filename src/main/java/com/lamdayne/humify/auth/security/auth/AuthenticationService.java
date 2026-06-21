package com.lamdayne.humify.auth.security.auth;

import com.lamdayne.humify.auth.dto.request.ForgotPasswordRequest;
import com.lamdayne.humify.auth.dto.request.ResetPasswordRequest;
import com.lamdayne.humify.auth.dto.request.SignInRequest;
import com.lamdayne.humify.auth.dto.response.TokenResponse;
import com.lamdayne.humify.auth.dto.response.UserMeResponse;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    TokenResponse login(SignInRequest request);

    TokenResponse refresh(HttpServletRequest request);

    void logout(HttpServletRequest request);

    void forgot(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void verifyCompany(HttpServletRequest request);

    void resendVerifyCompany(HttpServletRequest request);

    UserMeResponse me(UserPrincipal user);

}
