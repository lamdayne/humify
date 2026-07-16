package com.lamdayne.humify.branch.service;

import com.lamdayne.humify.branch.entity.Branch;
import com.lamdayne.humify.common.base.BaseAccessService;

import java.util.Optional;

public interface BranchAccessService extends BaseAccessService<Branch, Long> {

    boolean existsByIdAndCompanyId(Long id, Long companyId);

    Optional<Branch> findByName(String name);

    Branch save(Branch branch);
}
