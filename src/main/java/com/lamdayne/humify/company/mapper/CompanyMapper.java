package com.lamdayne.humify.company.mapper;

import com.lamdayne.humify.company.dto.request.CreateCompanyRequest;
import com.lamdayne.humify.company.dto.response.CompanyResponse;
import com.lamdayne.humify.company.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(CreateCompanyRequest request);

    CompanyResponse toCompanyResponse(Company company);

}
