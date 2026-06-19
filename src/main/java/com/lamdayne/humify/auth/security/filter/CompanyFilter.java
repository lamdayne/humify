package com.lamdayne.humify.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.enums.CompanyStatus;
import com.lamdayne.humify.company.service.CompanyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CompanyFilter extends OncePerRequestFilter {

    private final CompanyService companyService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String companyCode = request.getHeader("x-company-code");
            if (companyCode != null) {
                Company company = companyService.getCompanyByCode(companyCode);

                if (company.getStatus().equals(CompanyStatus.PENDING)) {
                    throw new AppException(ErrorCode.COMPANY_PENDING);
                }

                if (company.getStatus().equals(CompanyStatus.LOCKED)) {
                    throw new AppException(ErrorCode.COMPANY_LOCKED);
                }

                Long companyId = company.getId();
                CompanyContext.setCompanyId(companyId);
            }
        } catch (AppException e) {
            request.setAttribute("errorCode", e.getErrorCode());
            response.setStatus(e.getErrorCode().getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<?> apiResponse = ApiResponse.failure(e.getErrorCode());

            ObjectMapper mapper = new ObjectMapper();

            response.getWriter().write(mapper.writeValueAsString(apiResponse));
            response.getWriter().flush();
            return;
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            CompanyContext.clear();
        }
    }
}
