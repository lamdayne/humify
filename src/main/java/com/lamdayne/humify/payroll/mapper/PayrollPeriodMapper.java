package com.lamdayne.humify.payroll.mapper;


import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;
import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayrollPeriodMapper {

    @Mapping(source = "company.id", target = "companyId")
    PayrollPeriodResponse toResponse(PayrollPeriod payrollPeriod);
}
