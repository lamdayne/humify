package com.lamdayne.humify.payroll.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeContract;
import com.lamdayne.humify.employee.enums.ContractStatus;
import com.lamdayne.humify.employee.repository.EmployeeContractRepository;
import com.lamdayne.humify.payroll.dto.request.UpdatePayslipRequest;
import com.lamdayne.humify.payroll.dto.response.MyPayslipResponse;
import com.lamdayne.humify.payroll.dto.response.PayslipResponse;
import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.mapper.PayslipMapper;
import com.lamdayne.humify.payroll.repository.PayrollPeriodRepository;
import com.lamdayne.humify.payroll.repository.PayslipRepository;
import com.lamdayne.humify.payroll.util.PayrollTaxCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayslipServiceImplTest {

    @Mock
    private PayslipRepository payslipRepository;

    @Mock
    private PayrollPeriodRepository payrollPeriodRepository;

    @Mock
    private PayslipMapper payslipMapper;

    @Mock
    private EmployeeContractRepository employeeContractRepository;

    @InjectMocks
    private PayslipServiceImpl payslipService;

    private PayrollPeriod payrollPeriod;
    private Payslip payslip;
    private Employee employee;
    private EmployeeContract employeeContract;

    @BeforeEach
    void setUp() {

        payrollPeriod = mock(PayrollPeriod.class);

        employee = mock(Employee.class);

        employeeContract = mock(EmployeeContract.class);

        payslip = Payslip.builder()
                .payrollPeriod(payrollPeriod)
                .employee(employee)
                .baseSalary(new BigDecimal("15000000"))
                .standardWorkDays(new BigDecimal("26"))
                .actualWorkDays(new BigDecimal("26"))
                .paidLeaveDays(BigDecimal.ZERO)
                .unpaidLeaveDays(BigDecimal.ZERO)
                .salaryByWorkDays(new BigDecimal("15000000"))
                .totalAllowances(new BigDecimal("1000000"))
                .bonusKpi(new BigDecimal("500000"))
                .bonusProject(new BigDecimal("300000"))
                .otherBonuses(new BigDecimal("200000"))
                .otHours(new BigDecimal("2"))
                .otSalary(new BigDecimal("300000"))
                .grossSalary(new BigDecimal("17300000"))
                .deductionSocialInsurance(new BigDecimal("1200000"))
                .deductionHealthInsurance(new BigDecimal("225000"))
                .deductionUnemploymentInsurance(new BigDecimal("150000"))
                .personalIncomeTax(new BigDecimal("100000"))
                .otherDeductions(new BigDecimal("50000"))
                .netSalary(new BigDecimal("15575000"))
                .status(PayslipStatus.DRAFT)
                .build();
    }

    // =========================================================
    // getPayslipsByPeriod()
    // =========================================================

    @Test
    void getPayslipsByPeriod_success() {

        Long payrollPeriodId = 1L;

        PayslipResponse response = PayslipResponse.builder()
                .id(1L)
                .employeeId(1L)
                .employeeName("Nguyen Van A")
                .baseSalary(new BigDecimal("15000000"))
                .status(PayslipStatus.DRAFT)
                .build();

        Page<Payslip> page = new PageImpl<>(
                List.of(payslip),
                PageRequest.of(0, 10),
                1
        );

        when(payrollPeriodRepository.findById(payrollPeriodId))
                .thenReturn(Optional.of(payrollPeriod));

        when(payslipRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(response);

        PageResponse<PayslipResponse> result =
                payslipService.getPayslipsByPeriod(
                        payrollPeriodId,
                        null,
                        null,
                        0,
                        10
                );

        assertNotNull(result);
        assertEquals(0, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getItems().get(0).getId());

        verify(payrollPeriodRepository)
                .findById(payrollPeriodId);

        verify(payslipRepository)
                .findAll(any(Specification.class), any(Pageable.class));

        verify(payslipMapper)
                .toResponse(payslip);
    }

    @Test
    void getPayslipsByPeriod_payrollPeriodNotFound() {

        Long payrollPeriodId = 999L;

        when(payrollPeriodRepository.findById(payrollPeriodId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> payslipService.getPayslipsByPeriod(
                        payrollPeriodId,
                        null,
                        null,
                        0,
                        10
                )
        );

        assertEquals(
                ErrorCode.PAYROLL_PERIOD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(payrollPeriodRepository)
                .findById(payrollPeriodId);

        verify(payslipRepository, never())
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPayslipsByPeriod_filterByEmployeeId() {

        Long payrollPeriodId = 1L;
        Long employeeId = 10L;

        Page<Payslip> page = new PageImpl<>(
                List.of(payslip),
                PageRequest.of(0, 10),
                1
        );

        when(payrollPeriodRepository.findById(payrollPeriodId))
                .thenReturn(Optional.of(payrollPeriod));

        when(payslipRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(payslipMapper.toResponse(any(Payslip.class)))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(1L)
                                .employeeId(employeeId)
                                .build()
                );

        PageResponse<PayslipResponse> result =
                payslipService.getPayslipsByPeriod(
                        payrollPeriodId,
                        employeeId,
                        null,
                        0,
                        10
                );

        assertNotNull(result);
        assertEquals(1, result.getItems().size());

        verify(payslipRepository)
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPayslipsByPeriod_filterByStatus() {

        Long payrollPeriodId = 1L;

        Page<Payslip> page = new PageImpl<>(
                List.of(payslip),
                PageRequest.of(0, 10),
                1
        );

        when(payrollPeriodRepository.findById(payrollPeriodId))
                .thenReturn(Optional.of(payrollPeriod));

        when(payslipRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(payslipMapper.toResponse(any(Payslip.class)))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(1L)
                                .status(PayslipStatus.DRAFT)
                                .build()
                );

        PageResponse<PayslipResponse> result =
                payslipService.getPayslipsByPeriod(
                        payrollPeriodId,
                        null,
                        PayslipStatus.DRAFT,
                        0,
                        10
                );

        assertNotNull(result);
        assertEquals(1, result.getItems().size());

        verify(payslipRepository)
                .findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPayslipsByPeriod_filterByEmployeeAndStatus() {

        Long payrollPeriodId = 1L;
        Long employeeId = 1L;

        Page<Payslip> page = new PageImpl<>(
                List.of(payslip),
                PageRequest.of(0, 10),
                1
        );

        when(payrollPeriodRepository.findById(payrollPeriodId))
                .thenReturn(Optional.of(payrollPeriod));

        when(payslipRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(payslipMapper.toResponse(any(Payslip.class)))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(1L)
                                .employeeId(employeeId)
                                .status(PayslipStatus.DRAFT)
                                .build()
                );

        PageResponse<PayslipResponse> result =
                payslipService.getPayslipsByPeriod(
                        payrollPeriodId,
                        employeeId,
                        PayslipStatus.DRAFT,
                        0,
                        10
                );

        assertNotNull(result);
        assertEquals(1, result.getItems().size());

        verify(payslipRepository)
                .findAll(any(Specification.class), any(Pageable.class));
    }

    // =========================================================
    // updatePayslip()
    // =========================================================

    @Test
    void updatePayslip_success() {

        Long payslipId = 1L;

        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);

        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        new BigDecimal("1000000"),
                        new BigDecimal("200000"),
                        "Bonus tháng 8"
                );

        PayslipResponse response = PayslipResponse.builder()
                .id(payslipId)
                .otherBonuses(new BigDecimal("1000000"))
                .otherDeductions(new BigDecimal("200000"))
                .note("Bonus tháng 8")
                .build();

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(response);

        PayslipResponse result =
                payslipService.updatePayslip(
                        payslipId,
                        request
                );

        assertNotNull(result);

        assertEquals(
                new BigDecimal("1000000"),
                payslip.getOtherBonuses()
        );

        assertEquals(
                new BigDecimal("200000"),
                payslip.getOtherDeductions()
        );

        assertEquals(
                "Bonus tháng 8",
                payslip.getNote()
        );

        verify(payslipRepository)
                .findById(payslipId);

        verify(employeeContractRepository)
                .findByEmployeeIdAndStatus(
                        1L,
                        ContractStatus.ACTIVE
                );

        verify(payslipRepository)
                .save(payslip);

        verify(payslipMapper)
                .toResponse(payslip);
    }

    @Test
    void updatePayslip_notFound() {

        Long payslipId = 999L;

        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        new BigDecimal("1000000"),
                        new BigDecimal("200000"),
                        "Test"
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> payslipService.updatePayslip(
                        payslipId,
                        request
                )
        );

        assertEquals(
                ErrorCode.PAYSLIP_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(payslipRepository)
                .findById(payslipId);

        verify(payslipRepository, never())
                .save(any(Payslip.class));
    }

    @Test
    void updatePayslip_lockedPayrollPeriod() {

        Long payslipId = 1L;

        // Lấy một status khác DRAFT mà không cần biết
        // enum của project có tên cụ thể là gì.
        PayrollPeriodStatus lockedStatus =
                java.util.Arrays.stream(PayrollPeriodStatus.values())
                        .filter(status ->
                                status != PayrollPeriodStatus.DRAFT)
                        .findFirst()
                        .orElseThrow();

        when(payrollPeriod.getStatus())
                .thenReturn(lockedStatus);

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        new BigDecimal("1000000"),
                        new BigDecimal("200000"),
                        "Test"
                );

        AppException exception = assertThrows(
                AppException.class,
                () -> payslipService.updatePayslip(
                        payslipId,
                        request
                )
        );

        assertEquals(
                ErrorCode.PAYSLIP_LOCKED_FOR_EDIT,
                exception.getErrorCode()
        );

        verify(payslipRepository)
                .findById(payslipId);

        verify(payslipRepository, never())
                .save(any(Payslip.class));

        verify(employeeContractRepository, never())
                .findByEmployeeIdAndStatus(
                        anyLong(),
                        any(ContractStatus.class)
                );
    }

    @Test
    void updatePayslip_onlyOtherBonuses() {

        Long payslipId = 1L;
        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);
        BigDecimal oldOtherDeductions =
                payslip.getOtherDeductions();

        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        new BigDecimal("1500000"),
                        null,
                        null
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(PayslipResponse.builder()
                        .id(payslipId)
                        .build());

        payslipService.updatePayslip(
                payslipId,
                request
        );

        assertEquals(
                new BigDecimal("1500000"),
                payslip.getOtherBonuses()
        );

        assertEquals(
                oldOtherDeductions,
                payslip.getOtherDeductions()
        );


        verify(payslipRepository)
                .save(payslip);
    }

    @Test
    void updatePayslip_onlyOtherDeductions() {

        Long payslipId = 1L;

        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);
        BigDecimal oldOtherBonuses =
                payslip.getOtherBonuses();

        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        null,
                        new BigDecimal("500000"),
                        null
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(payslipId)
                                .build()
                );

        payslipService.updatePayslip(
                payslipId,
                request
        );

        assertEquals(
                oldOtherBonuses,
                payslip.getOtherBonuses()
        );

        assertEquals(
                new BigDecimal("500000"),
                payslip.getOtherDeductions()
        );

        verify(payslipRepository)
                .save(payslip);
    }

    @Test
    void updatePayslip_onlyNote() {

        Long payslipId = 1L;

        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);
        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        null,
                        null,
                        "Cập nhật ghi chú"
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(payslipId)
                                .note("Cập nhật ghi chú")
                                .build()
                );

        payslipService.updatePayslip(
                payslipId,
                request
        );

        assertEquals(
                "Cập nhật ghi chú",
                payslip.getNote()
        );

        verify(payslipRepository)
                .save(payslip);
    }

    @Test
    void updatePayslip_allRequestFieldsNull() {

        Long payslipId = 1L;

        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);
        BigDecimal oldBonuses =
                payslip.getOtherBonuses();

        BigDecimal oldDeductions =
                payslip.getOtherDeductions();

        String oldNote =
                payslip.getNote();

        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        null,
                        null,
                        null
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(payslipId)
                                .build()
                );

        payslipService.updatePayslip(
                payslipId,
                request
        );

        assertEquals(
                oldBonuses,
                payslip.getOtherBonuses()
        );

        assertEquals(
                oldDeductions,
                payslip.getOtherDeductions()
        );

        assertEquals(
                oldNote,
                payslip.getNote()
        );

        verify(payslipRepository)
                .save(payslip);
    }

    // =========================================================
    // Recalculate Gross Salary
    // =========================================================

    @Test
    void updatePayslip_shouldRecalculateGrossSalary() {

        Long payslipId = 1L;
        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);
        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        new BigDecimal("1000000"),
                        null,
                        null
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(payslipId)
                                .build()
                );

        payslipService.updatePayslip(
                payslipId,
                request
        );

        /*
         * Gross Salary =
         * salaryByWorkDays
         * + totalAllowances
         * + bonusKpi
         * + bonusProject
         * + otherBonuses
         * + otSalary
         */

        BigDecimal expectedGross =
                new BigDecimal("15000000")
                        .add(new BigDecimal("1000000"))
                        .add(new BigDecimal("500000"))
                        .add(new BigDecimal("300000"))
                        .add(new BigDecimal("1000000"))
                        .add(new BigDecimal("300000"));

        assertEquals(
                expectedGross,
                payslip.getGrossSalary()
        );
    }

    // =========================================================
    // Recalculate Net Salary
    // =========================================================

    @Test
    void updatePayslip_shouldRecalculateNetSalary() {

        Long payslipId = 1L;
        when(payrollPeriod.getStatus())
                .thenReturn(PayrollPeriodStatus.DRAFT);

        when(employee.getId())
                .thenReturn(1L);

        when(employeeContract.getTaxableDependents())
                .thenReturn(0);
        UpdatePayslipRequest request =
                new UpdatePayslipRequest(
                        new BigDecimal("1000000"),
                        new BigDecimal("200000"),
                        null
                );

        when(payslipRepository.findById(payslipId))
                .thenReturn(Optional.of(payslip));

        when(employeeContractRepository.findByEmployeeIdAndStatus(
                1L,
                ContractStatus.ACTIVE
        )).thenReturn(Optional.of(employeeContract));

        when(payslipRepository.save(payslip))
                .thenReturn(payslip);

        when(payslipMapper.toResponse(payslip))
                .thenReturn(
                        PayslipResponse.builder()
                                .id(payslipId)
                                .build()
                );

        payslipService.updatePayslip(
                payslipId,
                request
        );

        BigDecimal grossSalary =
                new BigDecimal("18100000");

        BigDecimal totalInsurance =
                new BigDecimal("1200000")
                        .add(new BigDecimal("225000"))
                        .add(new BigDecimal("150000"));

        BigDecimal taxableIncome =
                PayrollTaxCalculator.calculateTaxableIncome(
                        grossSalary,
                        0,
                        totalInsurance
                );

        BigDecimal expectedTax =
                PayrollTaxCalculator.calculatePersonalIncomeTax(
                        taxableIncome
                );

        BigDecimal expectedNet =
                grossSalary
                        .subtract(new BigDecimal("1200000"))
                        .subtract(new BigDecimal("225000"))
                        .subtract(new BigDecimal("150000"))
                        .subtract(expectedTax)
                        .subtract(new BigDecimal("200000"))
                        .setScale(2, java.math.RoundingMode.HALF_UP);

        assertEquals(
                expectedTax,
                payslip.getPersonalIncomeTax()
        );

        assertEquals(
                expectedNet,
                payslip.getNetSalary()
        );
    }

    // =========================================================
    // getMyPayslips()
    // =========================================================

    @Test
    void getMyPayslips_success() {

        Long employeeId = 1L;
        Integer year = 2026;

        MyPayslipResponse response =
                MyPayslipResponse.builder()
                        .id(1L)
                        .year(2026)
                        .month(8)
                        .baseSalary(new BigDecimal("15000000"))
                        .grossSalary(new BigDecimal("18000000"))
                        .netSalary(new BigDecimal("16000000"))
                        .status(PayslipStatus.SENT)
                        .build();

        payslip.setStatus(PayslipStatus.SENT);

        Page<Payslip> page =
                new PageImpl<>(
                        List.of(payslip),
                        PageRequest.of(0, 10),
                        1
                );

        when(payslipRepository.findMyPayslips(
                eq(employeeId),
                eq(List.of(
                        PayslipStatus.SENT,
                        PayslipStatus.PAID
                )),
                eq(year),
                any(Pageable.class)
        )).thenReturn(page);

        when(payslipMapper.toMyResponse(payslip))
                .thenReturn(response);

        PageResponse<MyPayslipResponse> result =
                payslipService.getMyPayslips(
                        employeeId,
                        year,
                        0,
                        10
                );

        assertNotNull(result);

        assertEquals(
                0,
                result.getPageNo()
        );

        assertEquals(
                10,
                result.getPageSize()
        );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                1,
                result.getItems().size()
        );

        assertEquals(
                1L,
                result.getItems().get(0).getId()
        );

        verify(payslipRepository)
                .findMyPayslips(
                        eq(employeeId),
                        eq(List.of(
                                PayslipStatus.SENT,
                                PayslipStatus.PAID
                        )),
                        eq(year),
                        any(Pageable.class)
                );

        verify(payslipMapper)
                .toMyResponse(payslip);
    }

    @Test
    void getMyPayslips_emptyResult() {

        Long employeeId = 1L;
        Integer year = 2026;

        Page<Payslip> emptyPage =
                new PageImpl<>(
                        List.of(),
                        PageRequest.of(0, 10),
                        0
                );

        when(payslipRepository.findMyPayslips(
                eq(employeeId),
                eq(List.of(
                        PayslipStatus.SENT,
                        PayslipStatus.PAID
                )),
                eq(year),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        PageResponse<MyPayslipResponse> result =
                payslipService.getMyPayslips(
                        employeeId,
                        year,
                        0,
                        10
                );

        assertNotNull(result);

        assertEquals(
                0,
                result.getTotalElements()
        );

        assertTrue(
                result.getItems().isEmpty()
        );

        verify(payslipRepository)
                .findMyPayslips(
                        eq(employeeId),
                        eq(List.of(
                                PayslipStatus.SENT,
                                PayslipStatus.PAID
                        )),
                        eq(year),
                        any(Pageable.class)
                );

        verify(payslipMapper, never())
                .toMyResponse(any(Payslip.class));
    }


}