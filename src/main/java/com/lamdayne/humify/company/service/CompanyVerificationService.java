package com.lamdayne.humify.company.service;

import com.lamdayne.humify.company.entity.CompanyVerification;

public interface CompanyVerificationService {

    void save(CompanyVerification companyVerification);

    CompanyVerification findByToken(String token);

}
