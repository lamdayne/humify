package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.entity.RefreshToken;
import com.lamdayne.humify.auth.repository.RefreshTokenRepository;
import com.lamdayne.humify.auth.service.RefreshTokenService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void save(String token, Long userId, Instant expiry) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiryDate(expiry)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeIfValid(String token) {
        int updated = refreshTokenRepository.revokeIfValid(token, Instant.now());

        if (updated == 0) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

}
