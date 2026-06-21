package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.auth.service.RoleAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.request.UpdateCompanyRequest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    //tạo email thành công
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

    @Test
    void createCompany_taxCodeExisted() {
        when(companyRepository.existsByTaxCode(request.getTaxCode()))
                .thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> companyService.createCompany(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_TAX_CODE_EXISTED);

        verify(companyRepository).existsByTaxCode(request.getTaxCode());
    }

    //tao email dã tồn tại
    @Test
    void createCompany_emailExisted() {
        when(companyRepository.existsByTaxCode(request.getTaxCode()))
                .thenReturn(false);

        when(companyRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> companyService.createCompany(request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_EMAIL_EXISTED);
    }

//approveCompany thành công
    @Test
    void approveCompany_success() {

        company.setCompanyCode("comp-code-123");

        when(companyRepository.findByCompanyCode("comp-code-123"))
                .thenReturn(Optional.of(company));

        companyService.approveCompany("comp-code-123");

        assertThat(company.getStatus()).isEqualTo(CompanyStatus.ACTIVE);

        verify(companyRepository).save(company);
    }
//approveCompany không tìm thấy company
    @Test
    void approveCompany_notFound() {

        when(companyRepository.findByCompanyCode("invalid-code"))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> companyService.approveCompany("invalid-code")
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_NOT_FOUND);

        verify(companyRepository, never()).save(any());
    }

    //getCompanyById thành công
    @Test
    void getCompanyById_success() {

        when(companyRepository.findById(10L))
                .thenReturn(Optional.of(company));

        Company result = companyService.getCompanyById(10L);

        assertNotNull(result);

        assertThat(result.getName())
                .isEqualTo(company.getName());
    }

    //getCompanyById thất bại
    @Test
    void getCompanyById_notFound() {

        when(companyRepository.findById(999L))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> companyService.getCompanyById(999L)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_NOT_FOUND);
    }

    //updateCompany thành công
    @Test
    void updateCompany_success() {

        UpdateCompanyRequest request = mock(UpdateCompanyRequest.class);

        when(request.getEmail()).thenReturn("new@gmail.com");

        company.setCompanyCode("comp-code-123");
        response.setEmail("new@gmail.com");

        when(companyRepository.existsByEmail("new@gmail.com"))
                .thenReturn(false);

        when(companyRepository.findByCompanyCode("comp-code-123"))
                .thenReturn(Optional.of(company));

        when(companyRepository.save(company))
                .thenReturn(company);

        when(companyMapper.toCompanyResponse(company))
                .thenReturn(response);

        CompanyResponse result =
                companyService.updateCompany("comp-code-123", request);

        assertNotNull(result);
        assertEquals("new@gmail.com", result.getEmail());

        verify(companyMapper).updateCompany(company, request);
        verify(companyRepository).save(company);
    }

    //email bị trùng

    @Test
    void updateCompany_emailExisted() {

        UpdateCompanyRequest request = mock(UpdateCompanyRequest.class);

        when(request.getEmail()).thenReturn("duplicate@gmail.com");
        when(companyRepository.existsByEmail("duplicate@gmail.com"))
                .thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> companyService.updateCompany("comp-code-123", request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_EMAIL_EXISTED);
    }

    //existsById
    @Test
    void existsById_success() {

        when(companyRepository.existsById(10L))
                .thenReturn(true);

        boolean result = companyService.existsById(10L);

        assertThat(result).isTrue();
    }


}
