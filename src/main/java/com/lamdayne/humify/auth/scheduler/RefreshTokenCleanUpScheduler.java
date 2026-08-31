package com.lamdayne.humify.auth.scheduler;

import com.lamdayne.humify.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanUpScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpExpiredTokens() {
        log.info("Start clean up expired tokens");
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Deleted {} expired tokens", deletedCount);
    }

}
