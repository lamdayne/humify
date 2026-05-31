package com.lamdayne.humify.auth.security.jwt;

import com.lamdayne.humify.auth.enums.TokenType;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessToken(UserPrincipal user);

    String generateRefreshToken(UserDetails user);

    String extractUsername(String token, TokenType type);

    boolean isValid(String token, TokenType type, UserDetails user);

    String extractCompanyId(String token, TokenType type);

}
