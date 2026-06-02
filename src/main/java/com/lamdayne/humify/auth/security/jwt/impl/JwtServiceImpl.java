package com.lamdayne.humify.auth.security.jwt.impl;

import com.lamdayne.humify.auth.enums.TokenType;
import com.lamdayne.humify.auth.security.jwt.JwtService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.util.SqidsUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final SqidsUtil sqidsUtil;

    @Value("${jwt.expiryAccessToken}")
    private int expiryAccessToken;

    @Value("${jwt.expiryDay}")
    private int expiryDay;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Override
    public String generateAccessToken(UserPrincipal user) {
        Map<String, Object> claims = new HashMap<>();
        if (user.getCompanyId() != null) {
            claims.put("companyId", sqidsUtil.encode(user.getCompanyId()));
        }

        return generateAccessToken(claims, user);
    }

    @Override
    public String generateRefreshToken(UserDetails user) {
        return generateRefreshToken(new HashMap<>(), user);
    }

    @Override
    public String extractUsername(String token, TokenType type) {
        return extractClaim(token, type, Claims::getSubject);
    }

    @Override
    public boolean isValid(String token, TokenType type, UserDetails user) {
        final String username = extractUsername(token, type);
        return username.equals(user.getUsername()) && !isTokenExpired(token, type);
    }

    @Override
    public String extractCompanyId(String token, TokenType type) {
        return extractClaim(token, type, claims -> claims.get("companyId").toString());
    }

    private String generateAccessToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(
                        Instant.now().plus(expiryAccessToken, ChronoUnit.MINUTES).toEpochMilli()
                ))
                .signWith(getKey(TokenType.ACCESS_TOKEN), SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(
                        Instant.now().plus(expiryDay, ChronoUnit.DAYS).toEpochMilli()
                ))
                .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS512)
                .compact();
    }

    private Key getKey(TokenType type) {
        return switch (type) {
            case ACCESS_TOKEN -> Keys.hmacShaKeyFor(secretKey.getBytes());
            case REFRESH_TOKEN -> Keys.hmacShaKeyFor(refreshKey.getBytes());
        };
    }

    private Claims extractAllClaims(String token, TokenType type) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T extractClaim(String token, TokenType type, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, type);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token, TokenType type) {
        return extractExpiration(token, type).before(new Date());
    }

    private Date extractExpiration(String token, TokenType type) {
        return extractClaim(token, type, Claims::getExpiration);
    }

}
