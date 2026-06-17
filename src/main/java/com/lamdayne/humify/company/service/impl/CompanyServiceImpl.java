package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.request.UpdateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.enums.CompanyStatus;
import com.lamdayne.humify.company.mapper.CompanyMapper;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.mail.dto.SendEmailEvent;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService, CompanyAccessService {

    @Value("${system.url}")
    private String SYSTEM_URL;

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher publisher;
    private final RoleAccessService roleAccessService;

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByTaxCode(request.getTaxCode())) {
            throw new AppException(ErrorCode.COMPANY_TAX_CODE_EXISTED);
        }

        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.COMPANY_EMAIL_EXISTED);
        }

        Company company = companyMapper.toCompany(request);
        String companyCode = UUID.randomUUID().toString();
        company.setCompanyCode(companyCode);

        company = companyRepository.save(company);

        String tempPassword = generateTempPassword();
        User user = User.builder()
                .email(company.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .active(Boolean.TRUE)
                .company(company)
                .build();

        user = userRepository.save(user);
        roleAccessService.assignCompanyAdmin(user);

        String urlLogin = String.format("%s/login?companyCode=%s", SYSTEM_URL, companyCode);

        Map<String, Object> variables = new HashMap<>();

        variables.put("companyCode", companyCode);
        variables.put("companyName", company.getName());
        variables.put("tempPassword", tempPassword);
        variables.put("companyEmail", company.getEmail());
        variables.put("urlLogin", urlLogin);

        publisher.publishEvent(
                SendEmailEvent.builder()
                        .to(company.getEmail())
                        .subject("[HRM] Account Activation Successful")
                        .templateId("9ab89648-1b1e-466e-a5e2-091bfd5dd873")
                        .variables(variables)
                        .build()
        );

        return companyMapper.toCompanyResponse(company);
    }

    @Override
    public PageResponse<CompanyResponse> getAllCompanies(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyResponse> companies = companyPage.stream().map(companyMapper::toCompanyResponse).toList();

        return PageResponse.<CompanyResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(companyPage.getTotalPages())
                .totalElements(companyPage.getTotalElements())
                .items(companies)
                .build();
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    @Transactional
    public void approveCompany(String companyCode) {
        Company company = companyRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        company.setStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company);
    }

    @Override
    public CompanyResponse updateCompany(String companyCode, UpdateCompanyRequest request) {
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.COMPANY_EMAIL_EXISTED);
        }

        Company company = companyRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        companyMapper.updateCompany(company, request);
        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    @Override
    public Company getCompanyByCode(String code) {
        return companyRepository.findByCompanyCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    public Company getReferenceById(Long id) {
        return companyRepository.getReferenceById(id);
    }

    @Override
    public Company getById(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long id) {
        return companyRepository.existsById(id);
    }

    @Override
    public Company findByCompanyCode(String companyCode) {
        return getCompanyByCode(companyCode);
    }

    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
