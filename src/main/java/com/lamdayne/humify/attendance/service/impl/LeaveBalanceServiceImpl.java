package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.entity.LeaveRequest;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;
import com.lamdayne.humify.attendance.repository.LeaveBalanceRepository;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveBalanceRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveBalanceResponse;
import com.lamdayne.humify.attendance.entity.LeaveBalance;
import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.repository.LeaveTypeRepository;
import com.lamdayne.humify.attendance.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyAccessService companyAccessService;

    @Override
    @Transactional
    public List<LeaveBalanceResponse> getLeaveBalances(Long employeeId, Integer year) {
        Long companyId = CompanyContext.getCompanyId();
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        Employee employee = employeeRepository.findById(employeeId)
                .filter(emp -> emp.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        List<LeaveType> activeTypes = leaveTypeRepository.findAllByCompanyId(companyId);
        Company company = companyAccessService.getReferenceById(companyId);

        for (LeaveType type : activeTypes) {
            boolean exists = leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(employeeId, type.getId(), targetYear);
            if (!exists) {
                BigDecimal defaultDays = (type.getMaxDays() != null) ? type.getMaxDays() : new BigDecimal("12.00");
                LeaveBalance newBalance = LeaveBalance.builder()
                        .company(company)
                        .employee(employee)
                        .leaveType(type)
                        .year(targetYear)
                        .allocatedDays(defaultDays)
                        .usedDays(new BigDecimal("0.00"))
                        .pendingDays(new BigDecimal("0.00"))
                        .build();
                leaveBalanceRepository.save(newBalance);
            }
        }

        List<LeaveBalance> balances = leaveBalanceRepository.findAllByEmployeeIdAndCompanyIdAndYear(employeeId, companyId, targetYear);

        return balances.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public LeaveBalanceResponse updateLeaveBalance(Long employeeId, UpdateLeaveBalanceRequest request) {
        Long companyId = CompanyContext.getCompanyId();

        LeaveBalance balance = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYearAndCompanyId(
                        employeeId, request.getLeaveTypeId(), request.getYear(), companyId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_BALANCE_NOT_FOUND));

        balance.setAllocatedDays(request.getAllocatedDays());
        return mapToResponse(leaveBalanceRepository.save(balance));
    }

    @Override
    @Transactional
    public void verifyAndReverseBalance(
            Long employeeId,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            LeaveSessionType sessionType,
            BigDecimal totalDurations
    ) {
        executeReverseBalance(employeeId, leaveType, startDate, endDate, sessionType, totalDurations);
    }

    @Override
    @Transactional
    public void updateLeaveBalanceForUpdate(Long employeeId, LeaveType leaveType, LeaveRequest oldRequest, UpdateLeaveRequest newRequest, BigDecimal newDuration) {
        if (leaveType.getMaxDays() == null) return;

        Map<Integer, BigDecimal> yearDurationMap = calculateDurationPerYear(
                oldRequest.getStartDate(),
                oldRequest.getEndDate(),
                oldRequest.getSessionType(),
                oldRequest.getDurationDays()
        );

        for (Map.Entry<Integer, BigDecimal> entry : yearDurationMap.entrySet()) {
            LeaveBalance balance = getBalance(employeeId, leaveType.getId(), entry.getKey());
            balance.setPendingDays(balance.getPendingDays().subtract(entry.getValue()));
            leaveBalanceRepository.save(balance);
        }

        LeaveSessionType newSession = newRequest.getSessionType() != null
                ? LeaveSessionType.valueOf(newRequest.getSessionType()) : LeaveSessionType.FULL_DAY;

        executeReverseBalance(employeeId, leaveType, newRequest.getStartDate(), newRequest.getEndDate(), newSession, newDuration);
    }

    @Override
    @Transactional
    public void save(LeaveBalance leaveBalance) {
        leaveBalanceRepository.save(leaveBalance);
    }

    @Override
    @Transactional
    public void releasePendingBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest) {
        subtractDaysFromBalance(employeeId, leaveType, leaveRequest, true);
    }

    @Override
    @Transactional
    public void releaseUsedBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest) {
        subtractDaysFromBalance(employeeId, leaveType, leaveRequest, false);
    }

    @Override
    @Transactional
    public void approveLeaveBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest) {
        if (leaveType.getMaxDays() == null) return;

        Map<Integer, BigDecimal> yearDurationMap = calculateDurationPerYear(
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getSessionType(),
                leaveRequest.getDurationDays()
        );

        for (Map.Entry<Integer, BigDecimal> entry : yearDurationMap.entrySet()) {
            LeaveBalance balance = getBalance(employeeId, leaveType.getId(), entry.getKey());
            balance.setPendingDays(balance.getPendingDays().subtract(entry.getValue()));
            balance.setUsedDays(balance.getUsedDays().add(entry.getValue()));
            leaveBalanceRepository.save(balance);
        }
    }

    @Override
    @Transactional
    public void rejectLeaveBalance(Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest) {
        subtractDaysFromBalance(employeeId, leaveType, leaveRequest, true);
    }

    private LeaveBalanceResponse mapToResponse(LeaveBalance balance) {
        BigDecimal remaining = balance.getAllocatedDays().subtract(balance.getUsedDays()).subtract(balance.getPendingDays());
        return LeaveBalanceResponse.builder()
                .id(balance.getId())
                .companyId(balance.getCompany().getId())
                .employeeId(balance.getEmployee().getId())
                .year(balance.getYear())
                .allocatedDays(balance.getAllocatedDays())
                .usedDays(balance.getUsedDays())
                .pendingDays(balance.getPendingDays())
                .remainingDays(remaining)
                .leaveType(LeaveBalanceResponse.LeaveTypeShortResponse.builder()
                        .id(balance.getLeaveType().getId())
                        .name(balance.getLeaveType().getName())
                        .code(balance.getLeaveType().getCode())
                        .build())
                .build();
    }

    private void subtractDaysFromBalance(
            Long employeeId, LeaveType leaveType, LeaveRequest leaveRequest, boolean isPending
    ) {
        if (leaveType.getMaxDays() == null) return;

        Map<Integer, BigDecimal> yearDurationMap = calculateDurationPerYear(
                leaveRequest.getStartDate(), leaveRequest.getEndDate(), leaveRequest.getSessionType(), leaveRequest.getDurationDays()
        );

        for (Map.Entry<Integer, BigDecimal> entry : yearDurationMap.entrySet()) {
            LeaveBalance balance = getBalance(employeeId, leaveType.getId(), entry.getKey());
            if (isPending) {
                balance.setPendingDays(balance.getPendingDays().subtract(entry.getValue()));
            } else {
                balance.setUsedDays(balance.getUsedDays().subtract(entry.getValue()));
            }
            leaveBalanceRepository.save(balance);
        }
    }

    private Map<Integer, BigDecimal> calculateDurationPerYear(
            LocalDate startDate, LocalDate endDate, LeaveSessionType sessionType, BigDecimal totalDuration
    ) {
        Map<Integer, BigDecimal> yearDurationMap = new HashMap<>();
        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        if (sessionType != LeaveSessionType.FULL_DAY && startYear != endYear) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_DATE_INVALID);
        }

        if (startYear == endYear) {
            yearDurationMap.put(startYear, totalDuration);
        } else {
            LocalDate endOfStartYear = LocalDate.of(startYear, 12, 31);
            long daysInOldYear = ChronoUnit.DAYS.between(startDate, endOfStartYear) + 1;
            BigDecimal oldYearDuration = BigDecimal.valueOf(daysInOldYear);
            BigDecimal newYearDuration = totalDuration.subtract(oldYearDuration);
            yearDurationMap.put(startYear, oldYearDuration);
            yearDurationMap.put(endYear, newYearDuration);
        }

        return yearDurationMap;
    }

    private void executeReverseBalance(
            Long employeeId,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate,
            LeaveSessionType sessionType,
            BigDecimal totalDuration
    ) {
        if (leaveType.getMaxDays() == null) return;

        Map<Integer, BigDecimal> yearDurationMap = calculateDurationPerYear(startDate, endDate, sessionType, totalDuration);

        for (Map.Entry<Integer, BigDecimal> entry : yearDurationMap.entrySet()) {
            LeaveBalance balance = getBalance(employeeId, leaveType.getId(), entry.getKey());
            BigDecimal availableDays = balance.getAllocatedDays()
                    .subtract(balance.getUsedDays()).subtract(balance.getPendingDays());

            if (entry.getValue().compareTo(availableDays) > 0) {
                throw new AppException(ErrorCode.LEAVE_BALANCE_INSUFFICIENT);
            }
        }

        for (Map.Entry<Integer, BigDecimal> entry : yearDurationMap.entrySet()) {
            LeaveBalance balance = getBalance(employeeId, leaveType.getId(), entry.getKey());
            balance.setPendingDays(balance.getPendingDays().add(entry.getValue()));
            leaveBalanceRepository.save(balance);
        }
    }

    private LeaveBalance getBalance(Long employeeId, Long leaveTypeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseGet(() -> {
                    Employee employee = employeeRepository.findById(employeeId)
                            .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

                    LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                            .orElseThrow(() -> new AppException(ErrorCode.LEAVE_TYPE_NOT_FOUND));

                    BigDecimal defaultDays = (leaveType.getMaxDays() != null) ? leaveType.getMaxDays() : new BigDecimal("12.00");

                    LeaveBalance newBalance = LeaveBalance.builder()
                            .company(employee.getCompany())
                            .employee(employee)
                            .leaveType(leaveType)
                            .year(year)
                            .allocatedDays(defaultDays)
                            .usedDays(new BigDecimal("0.00"))
                            .pendingDays(new BigDecimal("0.00"))
                            .build();

                    return leaveBalanceRepository.save(newBalance);
                });
    }

}