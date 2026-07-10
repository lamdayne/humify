package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.employee.dto.request.CreateContractRequest;
import com.lamdayne.humify.employee.dto.request.UpdateContractRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeContractResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeContract;
import com.lamdayne.humify.employee.enums.ContractStatus;
import com.lamdayne.humify.employee.mapper.EmployeeContractMapper;
import com.lamdayne.humify.employee.repository.EmployeeContractRepository;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeContractService;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeContractServiceImpl implements EmployeeContractService {

    private final EmployeeContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyAccessService companyAccessService;
    private final EmployeeContractMapper contractMapper;
    private final PayslipRepository payslipRepository;

    @Override
    @Transactional
    public EmployeeContractResponse createContract(CreateContractRequest request) {
        Long companyId = CompanyContext.getCompanyId();

        if (contractRepository.existsByCompanyIdAndContractNumberAndDeletedAtIsNull(companyId, request.getContractNumber())) {
            throw new AppException(ErrorCode.CONTRACT_NUMBER_EXISTED);
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .filter(emp -> emp.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (contractRepository.hasOverlapContract(request.getEmployeeId(), companyId, request.getStartDate(), request.getEndDate())) {
            throw new AppException(ErrorCode.CONTRACT_ALREADY_ACTIVE);
        }

        Company company = companyAccessService.getReferenceById(companyId);
        EmployeeContract contract = contractMapper.toEntity(request);

        contract.setCompany(company);
        contract.setEmployee(employee);
        contract.setStatus(ContractStatus.ACTIVE);

        if(contract.getAllowanceLunch() == null) contract.setAllowanceLunch(BigDecimal.ZERO);
        if(contract.getAllowancePhone() == null) contract.setAllowancePhone(BigDecimal.ZERO);
        if(contract.getAllowanceTransport() == null) contract.setAllowanceTransport(BigDecimal.ZERO);
        if(contract.getAllowanceOther() == null) contract.setAllowanceOther(BigDecimal.ZERO);
        if(contract.getTaxableDependents() == null) contract.setTaxableDependents(0);

        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    public PageResponse<EmployeeContractResponse> getContracts(Long employeeId, ContractStatus status, int page, int size, String... sorts) {
        Long companyId = CompanyContext.getCompanyId();
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);

        Page<EmployeeContract> contractPage = contractRepository.findWithFilters(companyId, employeeId, status, pageable);

        List<EmployeeContractResponse> items = contractPage.stream()
                .map(contractMapper::toResponse)
                .toList();

        return PageResponse.<EmployeeContractResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(contractPage.getTotalElements())
                .totalPages(contractPage.getTotalPages())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public EmployeeContractResponse updateContract(Long id, UpdateContractRequest request) {
        Long companyId = CompanyContext.getCompanyId();

        EmployeeContract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() == ContractStatus.TERMINATED) {
            throw new AppException(ErrorCode.CONTRACT_TERMINATED_CANNOT_EDIT);
        }

        contractMapper.updateEntity(contract, request);
        return contractMapper.toResponse(contractRepository.save(contract));
    }

    @Override
    @Transactional
    public void deleteContract(Long id) {
        Long companyId = CompanyContext.getCompanyId();

        EmployeeContract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        boolean hasPaidPayslip = payslipRepository.existsByEmployeeIdAndStatusIn(
                contract.getEmployee().getId(),
                Set.of(PayslipStatus.SENT, PayslipStatus.PAID)
        );

        if (hasPaidPayslip) {
            contract.setStatus(ContractStatus.TERMINATED);
            contract.setEndDate(LocalDate.now());
            contractRepository.save(contract);
        } else {
            contractRepository.delete(contract);
        }
    }
}