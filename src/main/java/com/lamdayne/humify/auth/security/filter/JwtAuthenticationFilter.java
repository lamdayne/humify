package com.lamdayne.humify.auth.security.filter;

import com.lamdayne.humify.auth.enums.TokenType;
import com.lamdayne.humify.auth.security.jwt.JwtService;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.util.SqidsUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SqidsUtil sqidsUtil;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final String authorization = request.getHeader("Authorization");

            if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authorization.substring("Bearer ".length());
            final String username = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);

            // check system admin
            Boolean isSystemAdmin = jwtService.isSystemAdmin(token, TokenType.ACCESS_TOKEN);
            if (Boolean.TRUE.equals(isSystemAdmin)) {
                CompanyContext.setCompanyId(null);
                CompanyContext.setAdmin(true);
            }

            // extract companyId and decode
            String companyId = jwtService.extractCompanyId(token, TokenType.ACCESS_TOKEN);
            if (companyId != null) {
                // Company user: set companyId and clear admin flag for tenant isolation
                CompanyContext.setCompanyId(sqidsUtil.decode(companyId));
                CompanyContext.setAdmin(false);
            }

            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isValid(token, TokenType.ACCESS_TOKEN, userDetails)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            request.setAttribute("errorCode", ErrorCode.JWT_EXPIRED);
            throw new InsufficientAuthenticationException(ErrorCode.JWT_EXPIRED.getCode(), e);
        }
    }
}
