package com.lamdayne.humify.branch.service.impl;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.branch.repository.BranchRepository;
import com.lamdayne.humify.branch.service.BranchService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    @Override
    public Branch getBranchById(Long id) {
        return  branchRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }
}
