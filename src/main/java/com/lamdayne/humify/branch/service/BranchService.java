package com.lamdayne.humify.branch.service;

import com.lamdayne.humify.branch.dto.request.CreateBranchRequest;
import com.lamdayne.humify.branch.dto.response.BranchResponse;
import com.lamdayne.humify.branch.entity.Branch;

public interface BranchService {
    Branch getBranchById(Long id);
    BranchResponse createBranch(CreateBranchRequest request);

}
