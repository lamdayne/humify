package com.lamdayne.humify.branch.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.request.UpdateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.mapper.BranchMapper;
import com.lamdayne.humify.branch.repository.BranchRepository;
import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService, BranchAccessService {

    private final BranchRepository branchRepository;
    private final CompanyService companyService;
    private final BranchMapper branchMapper;

    @Override
    @Transactional
    public BranchResponse createBranch(UserPrincipal userPrincipal, CreateBranchRequest request) {
        Company company = companyService.getCompanyById(userPrincipal.getCompanyId());

        Branch branch = branchMapper.toBranch(request);
        branch.setCompany(company);

        String branchCode = UUID.randomUUID().toString();
        branch.setBranchCode(branchCode);

        return branchMapper.toBranchResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponse updateBranch(Long id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        branch.setName(request.getName());
        branch.setField(request.getField());
        branch.setWebsite(request.getWebsite());
        branch.setAddress(request.getAddress());
        branch.setStandardHoursPerDay(request.getStandardHoursPerDay());
        branch.setStatus(com.lamdayne.humify.branch.enums.BranchStatus.valueOf(request.getStatus()));

        return branchMapper.toBranchResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        branch.setStatus(com.lamdayne.humify.branch.enums.BranchStatus.CLOSED);

        branchRepository.save(branch);
    }

    @Override
    public List<BranchResponse> getBranchesByCompanyId(Long companyId) {
        List<Branch> branches = branchRepository.findByCompanyId(companyId);
        return branchMapper.toBranchResponseList(branches);
    }

    @Override
    public PageResponse<BranchResponse> getAllBranches(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Branch> branches = branchRepository.findAll(pageable);

        List<BranchResponse> branchResponses = branches.stream().map(branchMapper::toBranchResponse).toList();

        return PageResponse.<BranchResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(branches.getTotalPages())
                .totalElements(branches.getTotalElements())
                .items(branchResponses)
                .build();
    }


    @Override
    public Branch getBranchById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    @Override
    public BranchResponse getBranchResponseById(Long id) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        return branchMapper.toBranchResponse(branch);
    }


    @Override
    public Branch getReferenceById(Long id) {
        return branchRepository.getReferenceById(id);
    }

    @Override
    public Branch getById(Long id) {
        return branchRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long id) {
        return branchRepository.existsById(id);
    }

    @Override
    public boolean existsByIdAndCompanyId(Long id, Long companyId) {
        return branchRepository.existsByIdAndCompanyId(id, companyId);
    }
}
