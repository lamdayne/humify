package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
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
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.repository.PayslipRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeContractServiceImpl Unit Tests")
class EmployeeContractServiceImplTest {

    @Mock private EmployeeContractRepository contractRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CompanyAccessService companyAccessService;
    @Mock private EmployeeContractMapper contractMapper;
    @Mock private PayslipRepository payslipRepository;

    @InjectMocks
    private EmployeeContractServiceImpl contractService;

    private Company company;
    private Employee employee;
    private EmployeeContract contract;

    @BeforeEach
    void setUp() {
        CompanyContext.setCompanyId(1L);

        company = Company.builder().build();
        company.setId(1L);

        employee = Employee.builder().company(company).build();
        employee.setId(2L);

        contract = EmployeeContract.builder()
                .company(company)
                .employee(employee)
                .contractNumber("HD-001")
                .contractType("OFFICIAL")
                .startDate(LocalDate.of(2026, 1, 1))
                .status(ContractStatus.ACTIVE)
                .build();
        contract.setId(3L);
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    // ---- createContract ----

    @Test
    @DisplayName("createContract - success, fills default allowances")
    void createContract_success() {
        CreateContractRequest request = mock(CreateContractRequest.class);
        when(request.getContractNumber()).thenReturn("HD-002");
        when(request.getEmployeeId()).thenReturn(2L);
        when(request.getStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(request.getEndDate()).thenReturn(null);

        EmployeeContract newContract = EmployeeContract.builder()
                .contractNumber("HD-002")
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        EmployeeContractResponse response = mock(EmployeeContractResponse.class);

        when(contractRepository.existsByCompanyIdAndContractNumberAndDeletedAtIsNull(1L, "HD-002")).thenReturn(false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(contractRepository.hasOverlapContract(eq(2L), eq(1L), any(), any())).thenReturn(false);
        when(companyAccessService.getReferenceById(1L)).thenReturn(company);
        when(contractMapper.toEntity(request)).thenReturn(newContract);
        when(contractRepository.save(newContract)).thenReturn(newContract);
        when(contractMapper.toResponse(newContract)).thenReturn(response);

        EmployeeContractResponse result = contractService.createContract(request);

        assertNotNull(result);
        assertThat(newContract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(newContract.getAllowanceLunch()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newContract.getAllowancePhone()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newContract.getAllowanceTransport()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newContract.getAllowanceOther()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(newContract.getTaxableDependents()).isEqualTo(0);
    }

    @Test
    @DisplayName("createContract - fail when contract number already exists")
    void createContract_contractNumberExists_throwsException() {
        CreateContractRequest request = mock(CreateContractRequest.class);
        when(request.getContractNumber()).thenReturn("HD-001");

        when(contractRepository.existsByCompanyIdAndContractNumberAndDeletedAtIsNull(1L, "HD-001")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> contractService.createContract(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONTRACT_NUMBER_EXISTED);
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("createContract - fail when employee not found or not in company")
    void createContract_employeeNotFound_throwsException() {
        CreateContractRequest request = mock(CreateContractRequest.class);
        when(request.getContractNumber()).thenReturn("HD-002");
        when(request.getEmployeeId()).thenReturn(999L);

        when(contractRepository.existsByCompanyIdAndContractNumberAndDeletedAtIsNull(1L, "HD-002")).thenReturn(false);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> contractService.createContract(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    @Test
    @DisplayName("createContract - fail when overlapping contract exists")
    void createContract_overlapContract_throwsException() {
        CreateContractRequest request = mock(CreateContractRequest.class);
        when(request.getContractNumber()).thenReturn("HD-002");
        when(request.getEmployeeId()).thenReturn(2L);
        when(request.getStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(request.getEndDate()).thenReturn(LocalDate.of(2026, 12, 31));

        when(contractRepository.existsByCompanyIdAndContractNumberAndDeletedAtIsNull(1L, "HD-002")).thenReturn(false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(contractRepository.hasOverlapContract(eq(2L), eq(1L), any(), any())).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> contractService.createContract(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONTRACT_ALREADY_ACTIVE);
        verify(contractRepository, never()).save(any());
    }

    // ---- getContracts ----

    @Test
    @DisplayName("getContracts - success")
    void getContracts_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeContract> page = new PageImpl<>(List.of(contract), pageable, 1);
        EmployeeContractResponse response = mock(EmployeeContractResponse.class);

        when(contractRepository.findWithFilters(eq(1L), eq(2L), eq(ContractStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(page);
        when(contractMapper.toResponse(contract)).thenReturn(response);

        PageResponse<EmployeeContractResponse> result =
                contractService.getContracts(2L, ContractStatus.ACTIVE, 0, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // ---- getContractById ----

    @Test
    @DisplayName("getContractById - success")
    void getContractById_success() {
        EmployeeContractResponse response = mock(EmployeeContractResponse.class);
        when(contractRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(contract));
        when(contractMapper.toResponse(contract)).thenReturn(response);

        EmployeeContractResponse result = contractService.getContractById(3L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getContractById - fail when not found")
    void getContractById_notFound_throwsException() {
        when(contractRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> contractService.getContractById(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONTRACT_NOT_FOUND);
    }

    // ---- updateContract ----

    @Test
    @DisplayName("updateContract - success")
    void updateContract_success() {
        UpdateContractRequest request = mock(UpdateContractRequest.class);
        EmployeeContractResponse response = mock(EmployeeContractResponse.class);

        when(contractRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(contract)).thenReturn(contract);
        when(contractMapper.toResponse(contract)).thenReturn(response);

        EmployeeContractResponse result = contractService.updateContract(3L, request);

        assertNotNull(result);
        verify(contractMapper).updateEntity(contract, request);
    }

    @Test
    @DisplayName("updateContract - fail when not found")
    void updateContract_notFound_throwsException() {
        UpdateContractRequest request = mock(UpdateContractRequest.class);
        when(contractRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> contractService.updateContract(999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONTRACT_NOT_FOUND);
    }

    @Test
    @DisplayName("updateContract - fail when contract already terminated")
    void updateContract_terminated_throwsException() {
        contract.setStatus(ContractStatus.TERMINATED);
        UpdateContractRequest request = mock(UpdateContractRequest.class);

        when(contractRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(contract));

        AppException exception = assertThrows(AppException.class,
                () -> contractService.updateContract(3L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONTRACT_TERMINATED_CANNOT_EDIT);
        verify(contractRepository, never()).save(any());
    }

    // ---- deleteContract ----

    @Test
    @DisplayName("deleteContract - success, hard delete when no paid payslip")
    void deleteContract_noPaidPayslip_hardDelete() {
        when(contractRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(contract));
        when(payslipRepository.existsByEmployeeIdAndStatusIn(eq(2L), anySet())).thenReturn(false);

        contractService.deleteContract(3L);

        verify(contractRepository).delete(contract);
        verify(contractRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteContract - success, soft delete (terminate) when has paid payslip")
    void deleteContract_hasPaidPayslip_softDelete() {
        when(contractRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(contract));
        when(payslipRepository.existsByEmployeeIdAndStatusIn(2L, Set.of(PayslipStatus.SENT, PayslipStatus.PAID)))
                .thenReturn(true);
        when(contractRepository.save(contract)).thenReturn(contract);

        contractService.deleteContract(3L);

        assertThat(contract.getStatus()).isEqualTo(ContractStatus.TERMINATED);
        assertThat(contract.getEndDate()).isEqualTo(LocalDate.now());
        verify(contractRepository).save(contract);
        verify(contractRepository, never()).delete(any(EmployeeContract.class));
    }

    @Test
    @DisplayName("deleteContract - fail when not found")
    void deleteContract_notFound_throwsException() {
        when(contractRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> contractService.deleteContract(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CONTRACT_NOT_FOUND);
    }
}