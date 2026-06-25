package com.lamdayne.humify.auth.security.auth.impl;

import com.lamdayne.humify.auth.dto.request.ForgotPasswordRequest;
import com.lamdayne.humify.auth.dto.request.ResetPasswordRequest;
import com.lamdayne.humify.auth.dto.request.SignInRequest;
import com.lamdayne.humify.auth.dto.response.SocialLoginResposne;
import com.lamdayne.humify.auth.dto.response.TokenResponse;
import com.lamdayne.humify.auth.dto.response.UserMeResponse;
import com.lamdayne.humify.auth.entity.PasswordResetToken;
import com.lamdayne.humify.auth.enums.AuthProvider;
import com.lamdayne.humify.auth.enums.TokenType;
import com.lamdayne.humify.auth.repository.PasswordResetTokenRepository;
import com.lamdayne.humify.auth.security.auth.AuthenticationService;
import com.lamdayne.humify.auth.security.jwt.JwtService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.service.RefreshTokenService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.mail.dto.SendEmailEvent;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.util.UriEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

    @Value("${system.url}")
    private String systemUrl;

    @Value("${resend.forgot-password.expiryTime}")
    private int forgotPasswordTtl;

    private final JwtService jwtService;
    private final UserService userService;
    private final CompanyService companyService;
    private final OAuth2ClientProperties properties;
    private final ApplicationEventPublisher publisher;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    @Transactional
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

        refreshTokenService.save(refreshToken, userPrincipal.getId(), Instant.now());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public TokenResponse refresh(HttpServletRequest request) {
        String token = request.getHeader("x-refresh-token");
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationServiceException("Token must be not blank");
        }

        final String email = jwtService.extractUsername(token, TokenType.REFRESH_TOKEN);

        refreshTokenService.revokeIfValid(token);

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        refreshTokenService.save(refreshToken, userPrincipal.getId(), Instant.now());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        String token = request.getHeader("x-refresh-token");
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationServiceException("Token must be not blank");
        }
        jwtService.extractUsername(token, TokenType.REFRESH_TOKEN);

        refreshTokenService.revokeIfValid(token);
    }

    @Override
    @Transactional
    public void forgot(ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVATED);
        }

        String resetToken = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(resetToken)
                .expiryTime(Instant.now().plus(forgotPasswordTtl, ChronoUnit.MINUTES))
                .userId(user.getId())
                .used(false)
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String resetUrl = String.format("%s/reset-password?token=%s", systemUrl, resetToken);

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", user.getEmail());
        variables.put("resetUrl", resetUrl);
        variables.put("expiryTime", forgotPasswordTtl);

        publisher.publishEvent(
                SendEmailEvent.builder()
                        .to(user.getEmail())
                        .subject("Reset password")
                        .templateId("3ea4bce3-e7e1-41e2-b3f7-eda117615732")
                        .variables(variables)
                        .build()
        );
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken passwordResetToken = resolveResetPassword(request);

        userService.resetPassword(passwordResetToken.getUserId(), request.getNewPassword());

        passwordResetToken.setUsed(Boolean.TRUE);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    @Transactional
    public void verifyCompany(HttpServletRequest request) {
        String token = request.getHeader("x-verify-token");
        if (StringUtils.isBlank(token)) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }

        companyService.verifyCompany(token);
    }

    @Override
    @Transactional
    public void resendVerifyCompany(HttpServletRequest request) {
        String token = request.getHeader("x-verify-token");
        if (StringUtils.isBlank(token)) {
            throw new AppException(ErrorCode.TOKEN_NOT_FOUND);
        }

        companyService.resendVerification(token);
    }

    @Override
    public UserMeResponse me(UserPrincipal user) {
        List<String> permissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String companyCode = null;
        if (user.getCompanyId() != null) {
            try {
                companyCode = companyService.getCompanyById(user.getCompanyId()).getCompanyCode();
            } catch (Exception ignored) {
                log.warn("Company code not found");
            }
        }

        return UserMeResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .companyId(user.getCompanyId())
                .companyCode(companyCode)
                .permissions(permissions)
                .build();
    }

    @Override
    public SocialLoginResposne generateLoginUrl(String type) {
        if (!type.equalsIgnoreCase(AuthProvider.GOOGLE.name())) {
            throw new AppException(ErrorCode.UNSUPPORTED_PROVIDER);
        }

        var registration = properties.getRegistration().get(AuthProvider.GOOGLE.name().toLowerCase());
        var provider = properties.getProvider().get(AuthProvider.GOOGLE.name().toLowerCase());

        String state = UUID.randomUUID().toString();

        String loginUrl = UriComponentsBuilder
                .fromUriString(provider.getAuthorizationUri())
                .queryParam("client_id", registration.getClientId())
                .queryParam("redirect_uri", registration.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", UriEncoder.encode(String.join(" ", registration.getScope())))
                .queryParam("state", state)
                .build()
                .toUriString();

        return SocialLoginResposne.builder()
                .loginUrl(loginUrl)
                .build();
    }

    private PasswordResetToken resolveResetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));

        if (passwordResetToken.getExpiryTime().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        if (Boolean.TRUE.equals(passwordResetToken.getUsed())) {
            throw new AppException(ErrorCode.RESET_TOKEN_USED);
        }

        return passwordResetToken;
    }

}
