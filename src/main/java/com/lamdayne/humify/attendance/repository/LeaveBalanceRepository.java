package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findAllByEmployeeIdAndCompanyIdAndYear(Long employeeId, Long companyId, Integer year);

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYearAndCompanyId(Long employeeId, Long leaveTypeId, Integer year, Long companyId);

    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, Integer year);
}
