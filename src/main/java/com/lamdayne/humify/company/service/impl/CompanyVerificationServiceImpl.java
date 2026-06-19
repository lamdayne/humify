package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.CompanyVerification;
import com.lamdayne.humify.company.repository.CompanyVerificationRepository;
import com.lamdayne.humify.company.service.CompanyVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyVerificationServiceImpl implements CompanyVerificationService {

    @Value("${resend.company-verification.expiryTime}")
    private int expiryTime;

    private final CompanyVerificationRepository companyVerificationRepository;

    @Override
    public void save(CompanyVerification companyVerification) {
        if (companyVerification.getExpiredAt() == null) {
            companyVerification.setExpiredAt(Instant.now().plus(expiryTime, ChronoUnit.MINUTES));
        }
        companyVerificationRepository.save(companyVerification);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyVerification findByToken(String token) {
        return companyVerificationRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
    }

}
