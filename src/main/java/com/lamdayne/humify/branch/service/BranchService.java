package com.lamdayne.humify.branch.service;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BranchService {
    Branch getBranchById(Long id);
    BranchResponse createBranch(CreateBranchRequest request);

    BranchResponse getBranchResponseById(Long id);
    List<BranchResponse> getBranchesByCompanyId(Long companyId);

    Page<BranchResponse> getAllBranches(int page, int size);
}
