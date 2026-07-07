package com.lamdayne.humify.attendance.service.impl;

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

import java.time.LocalDate;
import java.util.List;

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
                int defaultDays = (type.getMaxDays() != null) ? type.getMaxDays() : 12;
                LeaveBalance newBalance = LeaveBalance.builder()
                        .company(company)
                        .employee(employee)
                        .leaveType(type)
                        .year(targetYear)
                        .allocatedDays(defaultDays)
                        .usedDays(0)
                        .pendingDays(0)
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

    private LeaveBalanceResponse mapToResponse(LeaveBalance balance) {
        int remaining = balance.getAllocatedDays() - balance.getUsedDays() - balance.getPendingDays();
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
}