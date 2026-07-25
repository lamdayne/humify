package com.lamdayne.humify.payroll.mapper;


import com.lamdayne.humify.payroll.dto.request.UpdatePayslipRequest;
import com.lamdayne.humify.payroll.dto.response.MyPayslipResponse;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.entity.Payslip;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PayslipMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "employeeName", source = "employee.fullName")
    PayslipResponse toResponse(Payslip payslip);


    @Mapping(target = "payrollPeriodName", source = "payrollPeriod.name")
    @Mapping(target = "month", source = "payrollPeriod.month")
    @Mapping(target = "year", source = "payrollPeriod.year")
    @Mapping(target = "totalDeductions", expression = "java(calculateTotalDeductions(payslip))")
    MyPayslipResponse toMyResponse(Payslip payslip);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "payrollPeriod", ignore = true)
    @Mapping(target = "baseSalary", ignore = true)
    @Mapping(target = "standardWorkDays", ignore = true)
    @Mapping(target = "actualWorkDays", ignore = true)
    @Mapping(target = "paidLeaveDays", ignore = true)
    @Mapping(target = "unpaidLeaveDays", ignore = true)
    @Mapping(target = "salaryByWorkDays", ignore = true)
    @Mapping(target = "totalAllowances", ignore = true)
    @Mapping(target = "bonusKpi", ignore = true)
    @Mapping(target = "bonusProject", ignore = true)
    @Mapping(target = "otHours", ignore = true)
    @Mapping(target = "otSalary", ignore = true)
    @Mapping(target = "grossSalary", ignore = true)
    @Mapping(target = "deductionSocialInsurance", ignore = true)
    @Mapping(target = "deductionHealthInsurance", ignore = true)
    @Mapping(target = "deductionUnemploymentInsurance", ignore = true)
    @Mapping(target = "personalIncomeTax", ignore = true)
    @Mapping(target = "netSalary", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdatePayslipRequest request, @MappingTarget Payslip payslip);

    default BigDecimal calculateTotalDeductions(Payslip payslip) {
        return safe(payslip.getDeductionSocialInsurance())
                .add(safe(payslip.getDeductionHealthInsurance()))
                .add(safe(payslip.getDeductionUnemploymentInsurance()))
                .add(safe(payslip.getPersonalIncomeTax()))
                .add(safe(payslip.getOtherDeductions()));
    }

    default BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}