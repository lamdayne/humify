package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.CompanyVerification;
import com.lamdayne.humify.company.repository.CompanyVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyVerificationServiceImpl Tests")
public class CompanyVerificationServiceImplTest {

    @Mock
    private CompanyVerificationRepository companyVerificationRepository;

    @InjectMocks
    private CompanyVerificationServiceImpl companyVerificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(companyVerificationService, "expiryTime", 60);
    }

    @Test
    @DisplayName("Save company verification sets default expiredAt when null")
    void save_setsDefaultExpiredAt() {
        CompanyVerification verification = CompanyVerification.builder()
                .companyId(10L)
                .token("test-token")
                .expiredAt(null)
                .build();

        companyVerificationService.save(verification);

        ArgumentCaptor<CompanyVerification> captor = ArgumentCaptor.forClass(CompanyVerification.class);
        verify(companyVerificationRepository).save(captor.capture());

        CompanyVerification saved = captor.getValue();
        assertThat(saved.getExpiredAt()).isNotNull();
        // Since expiryTime is 60 minutes, expiredAt should be in the future
        assertThat(saved.getExpiredAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Save company verification keeps existing expiredAt when provided")
    void save_keepsExistingExpiredAt() {
        Instant preDefinedExpiry = Instant.now().plusSeconds(1800);
        CompanyVerification verification = CompanyVerification.builder()
                .companyId(10L)
                .token("test-token")
                .expiredAt(preDefinedExpiry)
                .build();

        companyVerificationService.save(verification);

        ArgumentCaptor<CompanyVerification> captor = ArgumentCaptor.forClass(CompanyVerification.class);
        verify(companyVerificationRepository).save(captor.capture());

        CompanyVerification saved = captor.getValue();
        assertThat(saved.getExpiredAt()).isEqualTo(preDefinedExpiry);
    }

    @Test
    @DisplayName("Find by token returns verification successfully")
    void findByToken_success() {
        CompanyVerification verification = CompanyVerification.builder()
                .companyId(10L)
                .token("test-token")
                .build();

        when(companyVerificationRepository.findByToken("test-token")).thenReturn(Optional.of(verification));

        CompanyVerification result = companyVerificationService.findByToken("test-token");

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("test-token");
    }

    @Test
    @DisplayName("Find by token throws exception when token not found")
    void findByToken_throwsWhenNotFound() {
        when(companyVerificationRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyVerificationService.findByToken("invalid-token"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.TOKEN_NOT_FOUND));
    }
}
