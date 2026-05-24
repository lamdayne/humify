package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.mapper.CompanyMapper;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        Company company = companyMapper.toCompany(request);
        String companyCode = UUID.randomUUID().toString().replace("-", "");
        company.setCompanyCode(companyCode);
        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }
}
