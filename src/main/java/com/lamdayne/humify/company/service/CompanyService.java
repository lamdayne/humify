package com.lamdayne.humify.company.service;

import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;

public interface CompanyService {
    CompanyResponse createCompany(CreateCompanyRequest request);
}
