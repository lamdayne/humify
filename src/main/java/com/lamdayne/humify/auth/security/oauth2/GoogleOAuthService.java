package com.lamdayne.humify.auth.security.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lamdayne.humify.auth.dto.request.SocialLoginRequest;
import com.lamdayne.humify.auth.dto.response.TokenResponse;
import com.lamdayne.humify.auth.entity.UserSocialAccount;
import com.lamdayne.humify.auth.enums.AuthProvider;
import com.lamdayne.humify.auth.repository.UserSocialAccountRepository;
import com.lamdayne.humify.auth.security.jwt.JwtService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Slf4j(topic = "GOOGLE_OAUTH_SERVICE")
@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final UserDetailsService userDetailsService;
    private final CompanyAccessService companyAccessService;
    private final TransactionTemplate transactionTemplate;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public TokenResponse authenticateAndFetchProfile(SocialLoginRequest request) {
        GoogleTokenResponse tokenResponse = exchange(request.getCode());

        GoogleIdToken idToken = verifyIdToken(tokenResponse.getIdToken());

        GoogleIdToken.Payload payload = idToken.getPayload();

        String email = payload.getEmail();
        String providerId = payload.getSubject();

        Company company = companyAccessService.findByCompanyCode(request.getCompanyCode());

        try {
            CompanyContext.setCompanyId(company.getId());
            return transactionTemplate.execute(status -> handleLogin(email, providerId, company));
        } finally {
            CompanyContext.setCompanyId(null);
        }
    }

    private GoogleTokenResponse exchange(String code) {
        var client = clientRegistrationRepository.findByRegistrationId("google");

        try {
            return new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    client.getClientId(),
                    client.getClientSecret(),
                    code,
                    client.getRedirectUri()
            ).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new AppException(ErrorCode.EXCHANGE_FAILED);
        }
    }

    private GoogleIdToken verifyIdToken(String idTokenString) {
        try {
            ClientRegistration client = clientRegistrationRepository.findByRegistrationId("google");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singleton(client.getClientId())).build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new AppException(ErrorCode.ID_TOKEN_INVALID);
            }

            return idToken;
        } catch (IOException | GeneralSecurityException e) {
            throw new AppException(ErrorCode.ID_TOKEN_INVALID);
        }
    }

    private TokenResponse handleLogin(String email, String providerId, Company company) {
        Optional<UserSocialAccount> socialOpt = userSocialAccountRepository.findByProviderAndProviderIdAndCompanyId(
                AuthProvider.GOOGLE.name(), providerId, company.getId()
        );

        User user;
        if (socialOpt.isPresent()) {
            user = socialOpt.get().getUser();
        } else {
            user = handleFirstLogin(email, providerId, company);
        }

        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private User handleFirstLogin(String email, String providerId, Company company) {
        Optional<User> userOpt = userService.findByEmailAndCompanyId(email, company.getId());

        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            if (Boolean.FALSE.equals(user.getActive())) {
                user.setActive(true);
                userService.save(user);
            }

            linkEmployee(user, email, company);
        } else {
            Employee employee = employeeRepository.findByEmailAndCompanyId(email, company.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

            user = User.builder()
                    .email(email)
                    .active(true)
                    .company(company)
                    .employee(employee)
                    .build();

            user = userService.createEmployeeUser(user);
        }

        createSocialAccount(user, providerId, company);

        return user;
    }

    private void createSocialAccount(User user, String providerId, Company company) {
        UserSocialAccount social = UserSocialAccount.builder()
                .user(user)
                .providerId(providerId)
                .company(company)
                .provider(AuthProvider.GOOGLE.name())
                .build();

        userSocialAccountRepository.save(social);
    }

    private void linkEmployee(User user, String email, Company company) {
        if (user.getEmployee() != null) return;

        employeeRepository.findByEmailAndCompanyId(email, company.getId())
                .ifPresent(emp -> {
                    if (user.getEmployee() == null) {
                        user.setEmployee(emp);
                        userService.save(user);
                    }
                });
    }

}
