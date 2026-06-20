package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.entity.CompanyVerification;
import com.lamdayne.humify.company.enums.CompanyStatus;
import com.lamdayne.humify.company.mapper.CompanyMapper;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyVerificationService;
import com.lamdayne.humify.mail.dto.SendEmailEvent;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private RoleAccessService roleAccessService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private CompanyVerificationService companyVerificationService;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private CreateCompanyRequest request;
    private CompanyResponse response;
    private Company company;
    private User user;

    @BeforeEach
    void setup() {
        request = CreateCompanyRequest.builder()
                .name("Company Name")
                .field("Technology")
                .taxCode("13423523452")
                .phone("123456789")
                .email("company@host.com")
                .build();

        response = CompanyResponse.builder()
                .name("Company Name")
                .field("Technology")
                .taxCode("13423523452")
                .phone("123456789")
                .email("company@host.com")
                .build();

        company = Company.builder()
                .name("Company Name")
                .field("Technology")
                .taxCode("13423523452")
                .phone("123456789")
                .email("company@host.com")
                .status(CompanyStatus.PENDING)
                .build();

        user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode("password"))
                .active(true)
                .company(company)
                .build();
    }

    @Test
    void createCompany_success() {
        when(companyRepository.existsByTaxCode(request.getTaxCode())).thenReturn(false);
        when(companyRepository.existsByEmail(request.getEmail())).thenReturn(false);

        when(companyMapper.toCompany(request)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);

        when(companyMapper.toCompanyResponse(company)).thenReturn(response);

        CompanyResponse companyResponse = companyService.createCompany(request);

        assertNotNull(companyResponse);
        assertThat(companyResponse.getTaxCode()).isEqualTo(request.getTaxCode());

        verify(companyRepository).save(company);
        verify(companyVerificationService).save(any(CompanyVerification.class));

        ArgumentCaptor<SendEmailEvent> captor = ArgumentCaptor.forClass(SendEmailEvent.class);

        verify(publisher).publishEvent(captor.capture());
    }

}
