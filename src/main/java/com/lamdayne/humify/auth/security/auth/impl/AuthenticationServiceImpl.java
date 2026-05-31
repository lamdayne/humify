package com.lamdayne.humify.auth.security.auth.impl;

import com.lamdayne.humify.auth.dto.request.SignInRequest;
import com.lamdayne.humify.auth.dto.response.TokenResponse;
import com.lamdayne.humify.auth.enums.TokenType;
import com.lamdayne.humify.auth.security.auth.AuthenticationService;
import com.lamdayne.humify.auth.security.jwt.JwtService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Override
    public TokenResponse login(SignInRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        // save to redis

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse refresh(HttpServletRequest request) {
        String token = request.getHeader("x-refresh-token");
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationServiceException("Token must be not blank");
        }

        final String email = jwtService.extractUsername(token, TokenType.REFRESH_TOKEN);
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

        if (!jwtService.isValid(token, TokenType.REFRESH_TOKEN, userPrincipal)) {
            throw new AuthenticationServiceException("Invalid token");
        }

        String accessToken = jwtService.generateAccessToken(userPrincipal);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(token)
                .build();
    }
}
