package com.lamdayne.humify.company.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.mapper.CompanyMapper;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        Company company = companyMapper.toCompany(request);
        String companyCode = UUID.randomUUID().toString().replace("-", "");
        company.setCompanyCode(companyCode);
        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    @Override
    public PageResponse<CompanyResponse> getAllCompanies(int page, int size, String sort) {
        int pageNo = page > 0 ? page - 1 : 0;

        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                }  else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(sorts));
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
}
