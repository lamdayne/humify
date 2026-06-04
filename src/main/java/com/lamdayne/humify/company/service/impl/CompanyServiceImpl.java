package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.request.UpdateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.enums.CompanyStatus;
import com.lamdayne.humify.company.mapper.CompanyMapper;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByTaxCode(request.getTaxCode())) {
            throw new AppException(ErrorCode.COMPANY_TAX_CODE_EXISTED);
        }

        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.COMPANY_EMAIL_EXISTED);
        }

        Company company = companyMapper.toCompany(request);
        String companyCode = UUID.randomUUID().toString();
        company.setCompanyCode(companyCode);
        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    @Override
    public PageResponse<CompanyResponse> getAllCompanies(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyResponse> companies = companyPage.stream().map(companyMapper::toCompanyResponse).toList();

        return PageResponse.<CompanyResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(companyPage.getTotalPages())
                .totalElements(companyPage.getTotalElements())
                .items(companies)
                .build();
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }

    @Override
    @Transactional
    public void approveCompany(String companyCode) {
        Company company = companyRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        company.setStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company);
    }

    @Override
    public CompanyResponse updateCompany(String companyCode, UpdateCompanyRequest request) {
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.COMPANY_EMAIL_EXISTED);
        }

        Company company = companyRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        companyMapper.updateCompany(company, request);
        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    @Override
    public Company getCompanyByCode(String code) {
        return companyRepository.findByCompanyCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
    }
}
