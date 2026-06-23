package com.lamdayne.humify.auth.service.impl;

import com.lamdayne.humify.auth.entity.PasswordResetToken;
import com.lamdayne.humify.auth.repository.PasswordResetTokenRepository;
import com.lamdayne.humify.auth.service.PasswordResetTokenService;
import com.lamdayne.humify.mail.dto.SendEmailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    @Value("${system.url}")
    private String systemUrl;

    @Value("${resend.set-password.expiryTime}")
    private int setPasswordTtl;

    private final ApplicationEventPublisher publisher;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public void setPasswordNewAccount(String email, Long userId, String name) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .userId(userId)
                .used(false)
                .expiryTime(Instant.now().plus(setPasswordTtl, ChronoUnit.MINUTES))
                .build();

        passwordResetTokenRepository.save(passwordResetToken);

        String setPasswordUrl = String.format("%s/set-password?token=%s", systemUrl, token);

        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("setPasswordUrl", setPasswordUrl);
        variables.put("expiryTime", setPasswordTtl);

        publisher.publishEvent(
                SendEmailEvent.builder()
                        .to(email)
                        .subject("Set password for new account")
                        .variables(variables)
                        .templateId("742b5145-c33c-4b89-abb0-1f63a38ebe06")
                        .build()
        );
    }
}
