package com.lamdayne.humify.payroll.service;

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
import com.lamdayne.humify.payroll.service.impl.PayrollPeriodServiceImpl;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollPeriodServiceImpl Tests")
class PayrollPeriodServiceImplTest {

    @Mock private EmployeeContractRepository employeeContractRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private PayslipRepository payslipRepository;
    @Mock private PayrollPeriodRepository payrollPeriodRepository;
    @Mock private CompanyService companyService;
    @Mock private PayrollPeriodMapper payrollPeriodMapper;

    @InjectMocks
    private PayrollPeriodServiceImpl payrollPeriodService;

    private Company company;
    private PayrollPeriod payrollPeriod;
    private PayrollPeriodResponse payrollPeriodResponse;
    private CreatePayrollPeriodRequest createRequest;

    @BeforeEach
    void setUp() {
        company = Company.builder().build();

        payrollPeriod = PayrollPeriod.builder()
                .company(company)
                .name("Thang 8/2026")
                .month(8)
                .year(2026)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 31))
                .standardWorkDays(new BigDecimal("22.00"))
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        payrollPeriodResponse = PayrollPeriodResponse.builder()
                .name("Thang 8/2026")
                .month(8)
                .year(2026)
                .status(PayrollPeriodStatus.DRAFT)
                .build();

        createRequest = new CreatePayrollPeriodRequest();
        createRequest.setName("Thang 8/2026");
        createRequest.setMonth(8);
        createRequest.setYear(2026);
        createRequest.setStartDate(LocalDate.of(2026, 8, 1));
        createRequest.setEndDate(LocalDate.of(2026, 8, 31));
        createRequest.setStandardWorkDays(new BigDecimal("22.00"));
    }

    // ---- createPayrollPeriod ----

    @Test
    @DisplayName("Create payroll period successfully")
    void createPayrollPeriod_success() {
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(payrollPeriodRepository.existsByCompanyIdAndMonthAndYear(1L, 8, 2026)).thenReturn(false);
        when(payrollPeriodRepository.save(any(PayrollPeriod.class))).thenReturn(payrollPeriod);
        when(payrollPeriodMapper.toResponse(payrollPeriod)).thenReturn(payrollPeriodResponse);

        PayrollPeriodResponse result = payrollPeriodService.createPayrollPeriod(1L, createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getMonth()).isEqualTo(8);
        verify(payrollPeriodRepository).save(any(PayrollPeriod.class));
    }

    @Test
    @DisplayName("Create payroll period throws exception when already exists")
    void createPayrollPeriod_throwsWhenAlreadyExists() {
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(payrollPeriodRepository.existsByCompanyIdAndMonthAndYear(1L, 8, 2026)).thenReturn(true);

        assertThatThrownBy(() -> payrollPeriodService.createPayrollPeriod(1L, createRequest))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_EXISTED));

        verify(payrollPeriodRepository, never()).save(any());
    }

    // ---- getPayrollPeriods ----

    @Test
    @DisplayName("Get payroll periods returns valid PageResponse")
    void getPayrollPeriods_success() {
        Page<PayrollPeriod> page = new PageImpl<>(List.of(payrollPeriod));
        when(payrollPeriodRepository.findByCompanyIdOrderByYearDescMonthDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(payrollPeriodMapper.toResponse(payrollPeriod)).thenReturn(payrollPeriodResponse);

        PageResponse<PayrollPeriodResponse> result = payrollPeriodService.getPayrollPeriods(1L, 1, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getPageNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get payroll periods returns empty page when no data")
    void getPayrollPeriods_emptyList() {
        Page<PayrollPeriod> page = new PageImpl<>(Collections.emptyList());
        when(payrollPeriodRepository.findByCompanyIdOrderByYearDescMonthDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<PayrollPeriodResponse> result = payrollPeriodService.getPayrollPeriods(1L, 2, 5);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getPageNo()).isEqualTo(2);
    }

    // ---- calculate ----

    @Test
    @DisplayName("Calculate throws exception when payroll period not found")
    void calculate_throwsWhenPeriodNotFound() {
        when(payrollPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollPeriodService.calculate(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));
    }

    @Test
    @DisplayName("Calculate throws exception when payroll period not in DRAFT status")
    void calculate_throwsWhenPeriodNotDraft() {
        payrollPeriod.setStatus(PayrollPeriodStatus.APPROVED);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));

        assertThatThrownBy(() -> payrollPeriodService.calculate(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_LOCKED));
    }

    @Test
    @DisplayName("Calculate returns zero when no active contracts")
    void calculate_returnsZeroWhenNoContracts() {
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(employeeContractRepository.findActiveContractsOverlappingPeriod(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        int result = payrollPeriodService.calculate(1L);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("Calculate payroll processes all attendance statuses successfully")
    void calculate_processesAllAttendanceStatuses() {
        Employee employee = new Employee();
        EmployeeContract contract = buildContract(employee, new BigDecimal("20000000"), new BigDecimal("20000000"));

        List<Attendance> attendances = List.of(
                buildAttendance(AttendanceStatus.PRESENT),
                buildAttendance(AttendanceStatus.LATE),
                buildAttendance(AttendanceStatus.REMOTE),
                buildAttendance(AttendanceStatus.HALF_DAY),
                buildAttendance(AttendanceStatus.LEAVE),
                buildAttendance(AttendanceStatus.ABSENT)
        );

        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(employeeContractRepository.findActiveContractsOverlappingPeriod(any(), any(), any()))
                .thenReturn(List.of(contract));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(any(), any(), any()))
                .thenReturn(attendances);
        when(payslipRepository.findByPayrollPeriod_IdAndEmployee_Id(any(), any()))
                .thenReturn(Optional.empty());
        when(payslipRepository.save(any(Payslip.class))).thenAnswer(inv -> inv.getArgument(0));

        int result = payrollPeriodService.calculate(1L);

        assertThat(result).isEqualTo(1);
        verify(payslipRepository).save(any(Payslip.class));
    }

    @Test
    @DisplayName("Calculate payroll updates existing payslip")
    void calculate_upsertsExistingPayslip() {
        Employee employee = new Employee();
        EmployeeContract contract = buildContract(employee, new BigDecimal("15000000"), new BigDecimal("15000000"));
        Payslip existingPayslip = Payslip.builder().employee(employee).payrollPeriod(payrollPeriod).build();

        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(employeeContractRepository.findActiveContractsOverlappingPeriod(any(), any(), any()))
                .thenReturn(List.of(contract));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(payslipRepository.findByPayrollPeriod_IdAndEmployee_Id(any(), any()))
                .thenReturn(Optional.of(existingPayslip));
        when(payslipRepository.save(any(Payslip.class))).thenAnswer(inv -> inv.getArgument(0));

        int result = payrollPeriodService.calculate(1L);

        assertThat(result).isEqualTo(1);
        ArgumentCaptor<Payslip> captor = ArgumentCaptor.forClass(Payslip.class);
        verify(payslipRepository).save(captor.capture());
        assertThat(captor.getValue()).isSameAs(existingPayslip);
    }

    @Test
    @DisplayName("Calculate payroll handles null taxable dependents")
    void calculate_handlesNullTaxableDependents() {
        Employee employee = new Employee();
        EmployeeContract contract = buildContract(employee, new BigDecimal("30000000"), new BigDecimal("20000000"));
        contract.setTaxableDependents(null);

        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(employeeContractRepository.findActiveContractsOverlappingPeriod(any(), any(), any()))
                .thenReturn(List.of(contract));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(any(), any(), any()))
                .thenReturn(List.of(buildAttendance(AttendanceStatus.PRESENT)));
        when(payslipRepository.findByPayrollPeriod_IdAndEmployee_Id(any(), any()))
                .thenReturn(Optional.empty());
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int result = payrollPeriodService.calculate(1L);

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("Calculate payroll handles high salary with 35% tax bracket")
    void calculate_highSalaryBracket() {
        Employee employee = new Employee();
        EmployeeContract contract = buildContract(employee, new BigDecimal("100000000"), new BigDecimal("50000000"));
        contract.setTaxableDependents(0);

        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(employeeContractRepository.findActiveContractsOverlappingPeriod(any(), any(), any()))
                .thenReturn(List.of(contract));
        when(attendanceRepository.findByEmployeeIdAndWorkDateBetween(any(), any(), any()))
                .thenReturn(List.of(buildAttendance(AttendanceStatus.PRESENT)));
        when(payslipRepository.findByPayrollPeriod_IdAndEmployee_Id(any(), any()))
                .thenReturn(Optional.empty());
        when(payslipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int result = payrollPeriodService.calculate(1L);
        assertThat(result).isEqualTo(1);
    }

    // ---- approve ----

    @Test
    @DisplayName("Approve payroll period successfully")
    void approve_success() {
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(payrollPeriodRepository.save(any())).thenReturn(payrollPeriod);

        payrollPeriodService.approve(1L);

        assertThat(payrollPeriod.getStatus()).isEqualTo(PayrollPeriodStatus.APPROVED);
        verify(payslipRepository).bulkUpdateStatusByPeriod(1L, PayslipStatus.DRAFT, PayslipStatus.SENT);
    }

    @Test
    @DisplayName("Approve throws exception when payroll period not found")
    void approve_throwsWhenNotFound() {
        when(payrollPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollPeriodService.approve(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));
    }

    @Test
    @DisplayName("Approve throws exception when payroll period not in DRAFT status")
    void approve_throwsWhenNotDraft() {
        payrollPeriod.setStatus(PayrollPeriodStatus.PAID);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));

        assertThatThrownBy(() -> payrollPeriodService.approve(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_NOT_APPROVABLE));
    }

    // ---- pay ----

    @Test
    @DisplayName("Pay payroll period successfully")
    void pay_success() {
        payrollPeriod.setStatus(PayrollPeriodStatus.APPROVED);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));
        when(payrollPeriodRepository.save(any())).thenReturn(payrollPeriod);

        payrollPeriodService.pay(1L);

        assertThat(payrollPeriod.getStatus()).isEqualTo(PayrollPeriodStatus.PAID);
        verify(payslipRepository).bulkMarkAsPaid(eq(1L), eq(PayslipStatus.SENT), eq(PayslipStatus.PAID), any(LocalDate.class));
    }

    @Test
    @DisplayName("Pay throws exception when payroll period not found")
    void pay_throwsWhenNotFound() {
        when(payrollPeriodRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payrollPeriodService.pay(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_NOT_FOUND));
    }

    @Test
    @DisplayName("Pay throws exception when payroll period not in APPROVED status")
    void pay_throwsWhenNotApproved() {
        payrollPeriod.setStatus(PayrollPeriodStatus.DRAFT);
        when(payrollPeriodRepository.findById(1L)).thenReturn(Optional.of(payrollPeriod));

        assertThatThrownBy(() -> payrollPeriodService.pay(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PAYROLL_PERIOD_NOT_PAYABLE));
    }

    // ---- Helpers ----

    private EmployeeContract buildContract(Employee employee, BigDecimal baseSalary, BigDecimal insuranceSalary) {
        Company c = Company.builder().build();
        EmployeeContract contract = new EmployeeContract();
        contract.setEmployee(employee);
        contract.setCompany(c);
        contract.setBaseSalary(baseSalary);
        contract.setInsuranceSalary(insuranceSalary);
        contract.setAllowanceLunch(new BigDecimal("500000"));
        contract.setAllowancePhone(new BigDecimal("300000"));
        contract.setAllowanceTransport(new BigDecimal("200000"));
        contract.setAllowanceOther(BigDecimal.ZERO);
        contract.setTaxableDependents(1);
        return contract;
    }

    private Attendance buildAttendance(AttendanceStatus status) {
        Attendance a = new Attendance();
        a.setStatus(status);
        return a;
    }
}