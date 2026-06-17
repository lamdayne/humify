package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.entity.RefreshToken;
import com.lamdayne.humify.auth.repository.RefreshTokenRepository;
import com.lamdayne.humify.auth.service.RefreshTokenService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
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
    @Transactional
    public void revokeIfValid(String token) {
        int updated = refreshTokenRepository.revokeIfValid(token, Instant.now());

        if (updated == 0) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

}
