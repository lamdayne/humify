package com.lamdayne.humify.auth.service;

import java.time.Instant;

public interface RefreshTokenService {

    void save(String token, Long userId, Instant expiry);

    void revokeIfValid(String token);

}
