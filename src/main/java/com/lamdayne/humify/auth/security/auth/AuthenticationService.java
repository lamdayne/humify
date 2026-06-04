package com.lamdayne.humify.auth.security.auth;

import com.lamdayne.humify.auth.dto.request.SignInRequest;
import com.lamdayne.humify.auth.dto.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    TokenResponse login(SignInRequest request);

    TokenResponse refresh(HttpServletRequest request);

    void logout(HttpServletRequest request);

}
