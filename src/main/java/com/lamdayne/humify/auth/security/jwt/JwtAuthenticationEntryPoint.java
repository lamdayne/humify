package com.lamdayne.humify.auth.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j(topic = "AUTHENTICATION_ENTRY_POINT")
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        log.error("Authentication Exception: {}", authException.getMessage());

        ErrorCode errorCode = resolveErrorCode(request, authException);

        writeError(response, errorCode);
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = ApiResponse.failure(errorCode);

        ObjectMapper mapper = new ObjectMapper();

        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }

    private ErrorCode resolveErrorCode(HttpServletRequest request, AuthenticationException ex) {
        Object attribute = request.getAttribute("errorCode");
        if (attribute instanceof ErrorCode ec) {
            return ec;
        }

        if (ex instanceof BadCredentialsException) {
            return ErrorCode.INVALID_PASSWORD;
        }

        if (ex instanceof InsufficientAuthenticationException) {
            return ErrorCode.UNAUTHENTICATED;
        }

        return ErrorCode.UNAUTHENTICATED;
    }
}
