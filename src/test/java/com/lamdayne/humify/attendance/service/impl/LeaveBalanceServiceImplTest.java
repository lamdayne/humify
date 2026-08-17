package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.UpdateLeaveBalanceRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.entity.LeaveBalance;
import com.lamdayne.humify.attendance.entity.LeaveRequest;
import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;
import com.lamdayne.humify.attendance.repository.LeaveBalanceRepository;
import com.lamdayne.humify.attendance.repository.LeaveTypeRepository;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.attendance.dto.response.LeaveBalanceResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveBalanceServiceImpl Unit Tests")
class LeaveBalanceServiceImplTest {

    @Mock private LeaveBalanceRepository leaveBalanceRepository;
    @Mock private LeaveTypeRepository leaveTypeRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CompanyAccessService companyAccessService;

    @InjectMocks
    private LeaveBalanceServiceImpl leaveBalanceService;

    private Company company;
    private Employee employee;
    private LeaveType leaveType;
    private LeaveBalance leaveBalance;

    @BeforeEach
    void setUp() {
        CompanyContext.setCompanyId(1L);

        company = Company.builder().build();
        company.setId(1L);
        employee = Employee.builder().company(company).build();
        employee.setId(2L);
        leaveType = LeaveType.builder().company(company).name("Annual Leave").code("AL")
                .maxDays(new BigDecimal("12.00")).build();
        leaveType.setId(3L);
        leaveBalance = LeaveBalance.builder()
                .company(company).employee(employee).leaveType(leaveType)
                .year(2026)
                .allocatedDays(new BigDecimal("12.00"))
                .usedDays(new BigDecimal("2.00"))
                .pendingDays(new BigDecimal("1.00"))
                .build();
        ;leaveBalance.setId(4L);
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    // ---- getLeaveBalances ----

    @Test
    @DisplayName("Get leave balances - success with data")
    void getLeaveBalances_createsMissingBalance_success() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findAllByCompanyId(1L)).thenReturn(List.of(leaveType));
        when(companyAccessService.getReferenceById(1L)).thenReturn(company);
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026)).thenReturn(false);
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(leaveBalanceRepository.findAllByEmployeeIdAndCompanyIdAndYear(2L, 1L, 2026))
                .thenReturn(List.of(leaveBalance));

        List<LeaveBalanceResponse> result = leaveBalanceService.getLeaveBalances(2L, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRemainingDays()).isEqualByComparingTo("9.00");
        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("getLeaveBalances - success, balance already exists, no creation, defaults to current year")
    void getLeaveBalances_existingBalance_noYear_success() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findAllByCompanyId(1L)).thenReturn(List.of(leaveType));
        when(companyAccessService.getReferenceById(1L)).thenReturn(company);
        when(leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(eq(2L), eq(3L), anyInt())).thenReturn(true);
        when(leaveBalanceRepository.findAllByEmployeeIdAndCompanyIdAndYear(eq(2L), eq(1L), anyInt()))
                .thenReturn(List.of(leaveBalance));

        List<LeaveBalanceResponse> result = leaveBalanceService.getLeaveBalances(2L, null);

        assertThat(result).hasSize(1);
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("getLeaveBalances - fail when employee not found or not in company")
    void getLeaveBalances_employeeNotFound_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> leaveBalanceService.getLeaveBalances(999L, 2026));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
    }

    // ---- updateLeaveBalance ----

    @Test
    @DisplayName("updateLeaveBalance - success")
    void updateLeaveBalance_success() {
        UpdateLeaveBalanceRequest request = mock(UpdateLeaveBalanceRequest.class);
        when(request.getLeaveTypeId()).thenReturn(3L);
        when(request.getYear()).thenReturn(2026);
        when(request.getAllocatedDays()).thenReturn(new BigDecimal("15.00"));

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndCompanyId(2L, 3L, 2026, 1L))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveBalanceResponse result = leaveBalanceService.updateLeaveBalance(2L, request);

        assertThat(result.getAllocatedDays()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("updateLeaveBalance - fail when balance not found")
    void updateLeaveBalance_notFound_throwsException() {
        UpdateLeaveBalanceRequest request = mock(UpdateLeaveBalanceRequest.class);
        when(request.getLeaveTypeId()).thenReturn(3L);
        when(request.getYear()).thenReturn(2026);

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndCompanyId(2L, 3L, 2026, 1L))
                .thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> leaveBalanceService.updateLeaveBalance(2L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_BALANCE_NOT_FOUND);
    }

    // ---- verifyAndReverseBalance ----

    @Test
    @DisplayName("verifyAndReverseBalance - success, sufficient balance, same year")
    void verifyAndReverseBalance_success() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.verifyAndReverseBalance(2L, leaveType,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1),
                LeaveSessionType.FULL_DAY, new BigDecimal("1.00"));

        verify(leaveBalanceRepository).save(argThat(b -> b.getPendingDays().compareTo(new BigDecimal("2.00")) == 0));
    }

    @Test
    @DisplayName("verifyAndReverseBalance - fail when balance insufficient")
    void verifyAndReverseBalance_insufficientBalance_throwsException() {
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));

        AppException exception = assertThrows(AppException.class,
                () -> leaveBalanceService.verifyAndReverseBalance(2L, leaveType,
                        LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1),
                        LeaveSessionType.FULL_DAY, new BigDecimal("100.00")));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_BALANCE_INSUFFICIENT);
        verify(leaveBalanceRepository, never()).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("verifyAndReverseBalance - skip when leaveType has no maxDays")
    void verifyAndReverseBalance_noMaxDays_skipped() {
        LeaveType unpaidType = LeaveType.builder().maxDays(null).build();
        unpaidType.setId(6L);

        leaveBalanceService.verifyAndReverseBalance(2L, unpaidType,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1),
                LeaveSessionType.FULL_DAY, new BigDecimal("1.00"));

        verifyNoInteractions(leaveBalanceRepository);
    }

    @Test
    @DisplayName("verifyAndReverseBalance - fail when partial-day leave spans two years")
    void verifyAndReverseBalance_partialDaySpansYears_throwsException() {
        AppException exception = assertThrows(AppException.class,
                () -> leaveBalanceService.verifyAndReverseBalance(2L, leaveType,
                        LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 1),
                        LeaveSessionType.MORNING, new BigDecimal("1.00")));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LEAVE_REQUEST_DATE_INVALID);
    }

    // ---- approveLeaveBalance ----

    @Test
    @DisplayName("approveLeaveBalance - success, moves pending to used")
    void approveLeaveBalance_success() {
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .sessionType(LeaveSessionType.FULL_DAY)
                .durationDays(new BigDecimal("1.00"))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.approveLeaveBalance(2L, leaveType, leaveRequest);

        verify(leaveBalanceRepository).save(argThat(b ->
                b.getPendingDays().compareTo(BigDecimal.ZERO) == 0
                        && b.getUsedDays().compareTo(new BigDecimal("3.00")) == 0));
    }

    // ---- releasePendingBalance / releaseUsedBalance / rejectLeaveBalance ----

    @Test
    @DisplayName("releasePendingBalance - success, subtracts from pendingDays")
    void releasePendingBalance_success() {
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .sessionType(LeaveSessionType.FULL_DAY)
                .durationDays(new BigDecimal("1.00"))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.releasePendingBalance(2L, leaveType, leaveRequest);

        verify(leaveBalanceRepository).save(argThat(b -> b.getPendingDays().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    @DisplayName("releaseUsedBalance - success, subtracts from usedDays")
    void releaseUsedBalance_success() {
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .sessionType(LeaveSessionType.FULL_DAY)
                .durationDays(new BigDecimal("2.00"))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.releaseUsedBalance(2L, leaveType, leaveRequest);

        verify(leaveBalanceRepository).save(argThat(b -> b.getUsedDays().compareTo(BigDecimal.ZERO) == 0));
    }

    @Test
    @DisplayName("rejectLeaveBalance - success, subtracts from pendingDays")
    void rejectLeaveBalance_success() {
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .sessionType(LeaveSessionType.FULL_DAY)
                .durationDays(new BigDecimal("1.00"))
                .build();

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.rejectLeaveBalance(2L, leaveType, leaveRequest);

        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
    }

    // ---- updateLeaveBalanceForUpdate ----

    @Test
    @DisplayName("updateLeaveBalanceForUpdate - success, reverses old and applies new duration")
    void updateLeaveBalanceForUpdate_success() {
        LeaveRequest oldRequest = LeaveRequest.builder()
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .sessionType(LeaveSessionType.FULL_DAY)
                .durationDays(new BigDecimal("1.00"))
                .build();
        UpdateLeaveRequest newRequest = mock(UpdateLeaveRequest.class);
        when(newRequest.getSessionType()).thenReturn("FULL_DAY");
        when(newRequest.getStartDate()).thenReturn(LocalDate.of(2026, 8, 2));
        when(newRequest.getEndDate()).thenReturn(LocalDate.of(2026, 8, 2));

        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(2L, 3L, 2026))
                .thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(inv -> inv.getArgument(0));

        leaveBalanceService.updateLeaveBalanceForUpdate(2L, leaveType, oldRequest, newRequest, new BigDecimal("2.00"));

        verify(leaveBalanceRepository, atLeast(2)).save(any(LeaveBalance.class));
    }

    @Test
    @DisplayName("updateLeaveBalanceForUpdate - skip when leaveType has no maxDays")
    void updateLeaveBalanceForUpdate_noMaxDays_skipped() {
        LeaveType unpaidType = LeaveType.builder().maxDays(null).build();
        unpaidType.setId(6L);
        LeaveRequest oldRequest = LeaveRequest.builder().build();
        UpdateLeaveRequest newRequest = mock(UpdateLeaveRequest.class);

        leaveBalanceService.updateLeaveBalanceForUpdate(2L, unpaidType, oldRequest, newRequest, BigDecimal.ONE);

        verifyNoInteractions(leaveBalanceRepository);
    }

    // ---- save ----

    @Test
    @DisplayName("save - success, delegates to repository")
    void save_success() {
        leaveBalanceService.save(leaveBalance);

        verify(leaveBalanceRepository).save(leaveBalance);
    }
}