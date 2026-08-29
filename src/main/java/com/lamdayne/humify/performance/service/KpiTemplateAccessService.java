package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.performance.entity.KpiTemplate;

public interface KpiTemplateAccessService {
    KpiTemplate getById(Long id);

    KpiTemplate getByIdAndCompanyId(
            Long id,
            Long companyId
    );
}
