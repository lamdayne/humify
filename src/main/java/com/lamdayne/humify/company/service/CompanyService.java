package com.lamdayne.humify.company.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;

public interface CompanyService {
    CompanyResponse createCompany(CreateCompanyRequest request);

    PageResponse<CompanyResponse> getAllCompanies(int page, int size, String sort);
    Company getCompanyById(Long id);
}
