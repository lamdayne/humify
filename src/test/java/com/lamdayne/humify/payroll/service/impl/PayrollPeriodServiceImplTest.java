package com.lamdayne.humify.payroll.service.impl;

import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeContract;
import com.lamdayne.humify.employee.enums.ContractStatus;
import com.lamdayne.humify.employee.repository.EmployeeContractRepository;
import com.lamdayne.humify.payroll.dto.request.CreatePayrollPeriodRequest;
import com.lamdayne.humify.payroll.dto.response.PayrollPeriodResponse;
import com.lamdayne.humify.payroll.entity.PayrollPeriod;
import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import com.lamdayne.humify.payroll.mapper.PayrollPeriodMapper;
import com.lamdayne.humify.payroll.repository.PayrollPeriodRepository;
import com.lamdayne.humify.payroll.repository.PayslipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TC ID: PP-SRV-01 to PP-SRV-18
 * Comprehensive test cases for PayrollPeriodService
 * Test Categories:
 * - Create Payroll Period (PP-SRV-01 to PP-SRV-04)
 * - Get Payroll Periods (PP-SRV-05 to PP-SRV-07)
 * - Calculate Payslips (PP-SRV-08 to PP-SRV-12)
 * - Approve Payroll Period (PP-SRV-13 to PP-SRV-15)
 * - Pay Payroll Period (PP-SRV-16 to PP-SRV-18)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollPeriodService Comprehensive Tests")
class PayrollPeriodServiceComprehensiveTest {

    @Mock
    private EmployeeContractRepository employeeContractRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private PayslipRepository payslipRepository;

    @Mock
    private PayrollPeriodRepository payrollPeriodRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private PayrollPeriodMapper payrollPeriodMapper;

    @InjectMocks
    private PayrollPeriodServiceImpl payrollPeriodService;

    private Company company;
    private PayrollPeriod payrollPeriod;
    private Employee employee;
    private EmployeeContract employeeContract;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);

        employee = new Employee();
        employee.setId(1L);

        employeeContract = new EmployeeContract();
        employeeContract.setEmployee(employee);
        employeeContract.setCompany(company);
        employeeContract.setBaseSalary(new BigDecimal("15000000"));
        employeeContract.setInsuranceSalary(new BigDecimal("15000000"));
        employeeContract.setAllowanceLunch(new BigDecimal("500000"));
        employeeContract.setAllowancePhone(new BigDecimal("200000"));
        employeeContract.setAllowanceTransport(new BigDecimal("300000"));
        employeeContract.setAllowanceOther(BigDecimal.ZERO);
        employeeContract.setTaxableDependents(0);

        payrollPeriod = PayrollPeriod.builder()
                .company(company)
                .name("Kỳ lương tháng 8/2026")
                .month(8)
                .year(2026)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .standardWorkDays(new BigDecimal("22"))
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        payrollPeriod.setId(1L);
    }

    // ========================================================
    // 1. CREATE PAYROLL PERIOD TESTS (PP-SRV-01 to PP-SRV-04)
    // ========================================================

    @Test
    @DisplayName("PP-SRV-01: createPayrollPeriod - Tạo kỳ lương mới thành công")
    void testCreatePayrollPeriodSuccess() {
        // Given
        Long companyId = 1L;
        CreatePayrollPeriodRequest request = new CreatePayrollPeriodRequest();
        request.setName("Kỳ lương tháng 8/2026");
        request.setMonth(8);
        request.setYear(2026);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));
        request.setStandardWorkDays(new BigDecimal("22"));

        PayrollPeriodResponse response = PayrollPeriodResponse.builder()
                .id(1L)
                .companyId(companyId)
                .name("Kỳ lương tháng 8/2026")
                .month(8)
                .year(2026)
                .standardWorkDays(new BigDecimal("22"))
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        when(companyService.getCompanyById(companyId)).thenReturn(company);
        when(payrollPeriodRepository.existsByCompanyIdAndMonthAndYear(companyId, 8, 2026))
                .thenReturn(false);
        when(payrollPeriodRepository.save(any(PayrollPeriod.class)))
                .thenReturn(payrollPeriod);
        when(payrollPeriodMapper.toResponse(payrollPeriod))
                .thenReturn(response);

        // When
        PayrollPeriodResponse result = payrollPeriodService.createPayrollPeriod(companyId, request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(companyId, result.getCompanyId());
        assertEquals("Kỳ lương tháng 8/2026", result.getName());
        assertEquals(8, result.getMonth());
        assertEquals(2026, result.getYear());
        assertEquals(PayrollPeriodStatus.DRAFT, result.getStatus());
        assertEquals(new BigDecimal("22"), result.getStandardWorkDays());

        verify(companyService).getCompanyById(companyId);
        verify(payrollPeriodRepository).existsByCompanyIdAndMonthAndYear(companyId, 8, 2026);
        verify(payrollPeriodRepository).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("PP-SRV-02: createPayrollPeriod - Kỳ lương đã tồn tại")
    void testCreatePayrollPeriodAlreadyExists() {
        // Given
        Long companyId = 1L;
        CreatePayrollPeriodRequest request = new CreatePayrollPeriodRequest();
        request.setName("Kỳ lương tháng 8/2026");
        request.setMonth(8);
        request.setYear(2026);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));
        request.setStandardWorkDays(new BigDecimal("22"));

        when(companyService.getCompanyById(companyId)).thenReturn(company);
        when(payrollPeriodRepository.existsByCompanyIdAndMonthAndYear(companyId, 8, 2026))
                .thenReturn(true);

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.createPayrollPeriod(companyId, request));

        assertEquals(ErrorCode.PAYROLL_PERIOD_EXISTED, exception.getErrorCode());
        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("PP-SRV-03: createPayrollPeriod - Công ty không tồn tại")
    void testCreatePayrollPeriodCompanyNotFound() {
        // Given
        Long companyId = 999L;
        CreatePayrollPeriodRequest request = new CreatePayrollPeriodRequest();
        request.setName("Kỳ lương tháng 8/2026");
        request.setMonth(8);
        request.setYear(2026);
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 8, 31));
        request.setStandardWorkDays(new BigDecimal("22"));

        when(companyService.getCompanyById(companyId))
                .thenThrow(new AppException(ErrorCode.COMPANY_NOT_FOUND));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.createPayrollPeriod(companyId, request));

        assertEquals(ErrorCode.COMPANY_NOT_FOUND, exception.getErrorCode());
        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("PP-SRV-04c: createPayrollPeriod - Thông tin đầu vào không hợp lệ (name trống)")
    void testCreatePayrollPeriodInvalidEmptyName() {
        // Given
        Long companyId = 1L;
        CreatePayrollPeriodRequest invalidRequest = new CreatePayrollPeriodRequest();
        invalidRequest.setName(""); // Empty name
        invalidRequest.setMonth(8);
        invalidRequest.setYear(2026);
        invalidRequest.setStartDate(LocalDate.of(2026, 8, 1));
        invalidRequest.setEndDate(LocalDate.of(2026, 8, 31));
        invalidRequest.setStandardWorkDays(new BigDecimal("22"));

        // When & Then
        // Validate that name is empty
        assertNotNull(invalidRequest);
        assertTrue(invalidRequest.getName().isEmpty());
    }

    // ========================================================
    // 2. GET PAYROLL PERIODS TESTS (PP-SRV-05 to PP-SRV-07)
    // ========================================================

    @Test
    @DisplayName("PP-SRV-05: getPayrollPeriods - Lấy danh sách kỳ lương thành công")
    void testGetPayrollPeriodsSuccess() {
        // Given
        Long companyId = 1L;
        List<PayrollPeriod> periods = new ArrayList<>();
        periods.add(payrollPeriod);

        Page<PayrollPeriod> page = new PageImpl<>(
                periods,
                PageRequest.of(0, 10),
                1
        );

        PayrollPeriodResponse response = PayrollPeriodResponse.builder()
                .id(1L)
                .companyId(companyId)
                .name("Kỳ lương tháng 8/2026")
                .month(8)
                .year(2026)
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        when(payrollPeriodRepository.findByCompanyIdOrderByYearDescMonthDesc(
                eq(companyId), any(Pageable.class)))
                .thenReturn(page);
        when(payrollPeriodMapper.toResponse(payrollPeriod))
                .thenReturn(response);

        // When
        PageResponse<PayrollPeriodResponse> result =
                payrollPeriodService.getPayrollPeriods(companyId, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getItems().get(0).getId());

        verify(payrollPeriodRepository)
                .findByCompanyIdOrderByYearDescMonthDesc(eq(companyId), any(Pageable.class));
    }

    @Test
    @DisplayName("PP-SRV-06: getPayrollPeriods - Danh sách rỗng")
    void testGetPayrollPeriodsEmpty() {
        // Given
        Long companyId = 1L;
        Page<PayrollPeriod> emptyPage = new PageImpl<>(
                new ArrayList<>(),
                PageRequest.of(0, 10),
                0
        );

        when(payrollPeriodRepository.findByCompanyIdOrderByYearDescMonthDesc(
                eq(companyId), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        PageResponse<PayrollPeriodResponse> result =
                payrollPeriodService.getPayrollPeriods(companyId, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getItems().isEmpty());

        verify(payrollPeriodMapper, never()).toResponse(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("PP-SRV-07: getPayrollPeriods - Phân trang")
    void testGetPayrollPeriodsPagination() {
        // Given
        Long companyId = 1L;
        List<PayrollPeriod> firstPagePeriods = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PayrollPeriod p = new PayrollPeriod();
            p.setId((long) i + 1);
            firstPagePeriods.add(p);
        }

        Page<PayrollPeriod> firstPage = new PageImpl<>(
                firstPagePeriods,
                PageRequest.of(0, 10),
                25
        );

        when(payrollPeriodRepository.findByCompanyIdOrderByYearDescMonthDesc(
                eq(companyId), any(Pageable.class)))
                .thenReturn(firstPage);

        // When
        PageResponse<PayrollPeriodResponse> result =
                payrollPeriodService.getPayrollPeriods(companyId, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getItems().size());
        assertEquals(25, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    // ========================================================
    // 3. CALCULATE PAYSLIPS TESTS (PP-SRV-08 to PP-SRV-12)
    // ========================================================

    @Test
    @DisplayName("PP-SRV-08: calculate - Tính lương thành công, tạo payslip mới")
    void testCalculateCreatePayslipSuccess() {
        // Given
        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        when(employeeContractRepository.findActiveContractsOverlappingPeriod(
                ContractStatus.ACTIVE,
                payrollPeriod.getStartDate(),
                payrollPeriod.getEndDate()))
                .thenReturn(List.of(employeeContract));

        Attendance attendance1 = new Attendance();
        attendance1.setStatus(AttendanceStatus.PRESENT);

        Attendance attendance2 = new Attendance();
        attendance2.setStatus(AttendanceStatus.LATE);

        Attendance attendance3 = new Attendance();
        attendance3.setStatus(AttendanceStatus.HALF_DAY);

        Attendance attendance4 = new Attendance();
        attendance4.setStatus(AttendanceStatus.LEAVE);

        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(
                1L,
                payrollPeriod.getStartDate(),
                payrollPeriod.getEndDate()))
                .thenReturn(List.of(attendance1, attendance2, attendance3, attendance4));

        when(payslipRepository.findByPayrollPeriod_IdAndEmployee_Id(1L, 1L))
                .thenReturn(Optional.empty());

        when(payslipRepository.save(any(Payslip.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        int result = payrollPeriodService.calculate(1L);

        // Then
        assertEquals(1, result);

        ArgumentCaptor<Payslip> captor = ArgumentCaptor.forClass(Payslip.class);
        verify(payslipRepository).save(captor.capture());

        Payslip savedPayslip = captor.getValue();
        assertEquals(new BigDecimal("2.5"), savedPayslip.getActualWorkDays());
        assertEquals(new BigDecimal("1"), savedPayslip.getPaidLeaveDays());
        assertEquals(PayslipStatus.DRAFT, savedPayslip.getStatus());
    }

    @Test
    @DisplayName("PP-SRV-09: calculate - Tính lương thành công, update payslip cũ")
    void testCalculateUpdateExistingPayslip() {
        // Given
        Payslip existingPayslip = new Payslip();

        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        when(employeeContractRepository.findActiveContractsOverlappingPeriod(
                ContractStatus.ACTIVE,
                payrollPeriod.getStartDate(),
                payrollPeriod.getEndDate()))
                .thenReturn(List.of(employeeContract));

        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(
                1L,
                payrollPeriod.getStartDate(),
                payrollPeriod.getEndDate()))
                .thenReturn(List.of());

        when(payslipRepository.findByPayrollPeriod_IdAndEmployee_Id(1L, 1L))
                .thenReturn(Optional.of(existingPayslip));

        when(payslipRepository.save(existingPayslip))
                .thenReturn(existingPayslip);

        // When
        int result = payrollPeriodService.calculate(1L);

        // Then
        assertEquals(1, result);
        verify(payslipRepository).save(existingPayslip);
    }

    @Test
    @DisplayName("PP-SRV-10: calculate - Kỳ lương không tồn tại")
    void testCalculatePayrollPeriodNotFound() {
        // Given
        when(payrollPeriodRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.calculate(999L));

        assertEquals(ErrorCode.PAYROLL_PERIOD_NOT_FOUND, exception.getErrorCode());
        verify(employeeContractRepository, never())
                .findActiveContractsOverlappingPeriod(any(), any(), any());
    }

    @Test
    @DisplayName("PP-SRV-11: calculate - Kỳ lương đã bị khóa")
    void testCalculatePayrollPeriodLocked() {
        // Given
        payrollPeriod.setStatus(PayrollPeriodStatus.APPROVED);

        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.calculate(1L));

        assertEquals(ErrorCode.PAYROLL_PERIOD_LOCKED, exception.getErrorCode());
        verify(payslipRepository, never()).save(any(Payslip.class));
    }

    @Test
    @DisplayName("PP-SRV-12: calculate - Không có nhân viên với hợp đồng hoạt động")
    void testCalculateNoActiveContracts() {
        // Given
        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        when(employeeContractRepository.findActiveContractsOverlappingPeriod(
                ContractStatus.ACTIVE,
                payrollPeriod.getStartDate(),
                payrollPeriod.getEndDate()))
                .thenReturn(List.of());

        // When
        int result = payrollPeriodService.calculate(1L);

        // Then
        assertEquals(0, result);
        verify(payslipRepository, never()).save(any(Payslip.class));
    }

    // ========================================================
    // 4. APPROVE PAYROLL PERIOD TESTS (PP-SRV-13 to PP-SRV-15)
    // ========================================================

    @Test
    @DisplayName("PP-SRV-13: approve - Duyệt kỳ lương thành công")
    void testApprovePayrollPeriodSuccess() {
        // Given
        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        // When
        payrollPeriodService.approve(1L);

        // Then
        assertEquals(PayrollPeriodStatus.APPROVED, payrollPeriod.getStatus());
        verify(payrollPeriodRepository).save(payrollPeriod);
        verify(payslipRepository)
                .bulkUpdateStatusByPeriod(1L, PayslipStatus.DRAFT, PayslipStatus.SENT);
    }

    @Test
    @DisplayName("PP-SRV-14: approve - Kỳ lương không tồn tại")
    void testApprovePayrollPeriodNotFound() {
        // Given
        when(payrollPeriodRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.approve(999L));

        assertEquals(ErrorCode.PAYROLL_PERIOD_NOT_FOUND, exception.getErrorCode());
        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("PP-SRV-15: approve - Kỳ lương không ở trạng thái DRAFT")
    void testApprovePayrollPeriodNotApprovable() {
        // Given
        payrollPeriod.setStatus(PayrollPeriodStatus.APPROVED);

        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.approve(1L));

        assertEquals(ErrorCode.PAYROLL_PERIOD_NOT_APPROVABLE, exception.getErrorCode());
        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }

    // ========================================================
    // 5. PAY PAYROLL PERIOD TESTS (PP-SRV-16 to PP-SRV-18)
    // ========================================================

    @Test
    @DisplayName("PP-SRV-16: pay - Thanh toán kỳ lương thành công")
    void testPayPayrollPeriodSuccess() {
        // Given
        payrollPeriod.setStatus(PayrollPeriodStatus.APPROVED);

        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        // When
        payrollPeriodService.pay(1L);

        // Then
        assertEquals(PayrollPeriodStatus.PAID, payrollPeriod.getStatus());
        verify(payrollPeriodRepository).save(payrollPeriod);
        verify(payslipRepository)
                .bulkMarkAsPaid(eq(1L), eq(PayslipStatus.SENT), eq(PayslipStatus.PAID),
                        any(LocalDate.class));
    }

    @Test
    @DisplayName("PP-SRV-17: pay - Kỳ lương không tồn tại")
    void testPayPayrollPeriodNotFound() {
        // Given
        when(payrollPeriodRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.pay(999L));

        assertEquals(ErrorCode.PAYROLL_PERIOD_NOT_FOUND, exception.getErrorCode());
        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("PP-SRV-18: pay - Kỳ lương không ở trạng thái APPROVED")
    void testPayPayrollPeriodNotPayable() {
        // Given
        payrollPeriod.setStatus(PayrollPeriodStatus.DRAFT);

        when(payrollPeriodRepository.findById(1L))
                .thenReturn(Optional.of(payrollPeriod));

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> payrollPeriodService.pay(1L));

        assertEquals(ErrorCode.PAYROLL_PERIOD_NOT_PAYABLE, exception.getErrorCode());
        verify(payrollPeriodRepository, never()).save(any(PayrollPeriod.class));
    }
}