package com.lamdayne.humify.company.service;

import com.lamdayne.humify.common.base.BaseAccessService;
import com.lamdayne.humify.company.entity.Company;

public interface CompanyAccessService extends BaseAccessService<Company, Long> {

    Company findByCompanyCode(String companyCode);

}
