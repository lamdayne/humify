package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.entity.RefreshToken;
import com.lamdayne.humify.auth.repository.RefreshTokenRepository;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenServiceImpl Tests")
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    @DisplayName("Save refresh token successfully")
    void save_success() {
        String token = "dummy-token";
        Long userId = 123L;
        Instant expiry = Instant.now().plusSeconds(3600);

        refreshTokenService.save(token, userId, expiry);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertThat(savedToken.getToken()).isEqualTo(token);
        assertThat(savedToken.getUserId()).isEqualTo(userId);
        assertThat(savedToken.getExpiryDate()).isEqualTo(expiry);
        assertThat(savedToken.getRevoked()).isFalse();
    }

    @Test
    @DisplayName("Revoke token successfully when valid")
    void revokeIfValid_success() {
        String token = "valid-token";
        when(refreshTokenRepository.revokeIfValid(eq(token), any(Instant.class))).thenReturn(1);

        refreshTokenService.revokeIfValid(token);

        verify(refreshTokenRepository).revokeIfValid(eq(token), any(Instant.class));
    }

    @Test
    @DisplayName("Revoke token throws exception when invalid")
    void revokeIfValid_throwsWhenInvalid() {
        String token = "invalid-token";
        when(refreshTokenRepository.revokeIfValid(eq(token), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> refreshTokenService.revokeIfValid(token))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));

        verify(refreshTokenRepository).revokeIfValid(eq(token), any(Instant.class));
    }
}
