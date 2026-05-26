package com.lamdayne.humify.branch.service.impl;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.mapper.BranchMapper;
import com.lamdayne.humify.branch.repository.BranchRepository;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private final CompanyService companyService;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        Company company = companyService.getCompanyById(request.getCompanyId());

        Branch branch = branchMapper.toBranch(request);
        branch.setCompany(company);

        String BranchCode = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        branch.setBranchCode(BranchCode);

        return branchMapper.toBranchResponse(branchRepository.save(branch));
    }
    @Override
    public Branch getBranchById(Long id) {
        return  branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }
}
