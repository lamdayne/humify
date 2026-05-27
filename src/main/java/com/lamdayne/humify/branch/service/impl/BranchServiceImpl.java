package com.lamdayne.humify.branch.service.impl;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.mapper.BranchMapper;
import com.lamdayne.humify.branch.repository.BranchRepository;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
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
    public List<BranchResponse> getBranchesByCompanyId(Long companyId) {
        List<Branch> branches = branchRepository.findByCompanyId(companyId);
        return branchMapper.toBranchResponseList(branches);
    }

    @Override
    public PageResponse<BranchResponse> getAllBranches(int page, int size, String sort) {
        int pageNo = page > 0 ? page - 1 : 0;

        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(sorts));
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


}
