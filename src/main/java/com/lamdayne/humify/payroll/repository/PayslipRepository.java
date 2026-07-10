package com.lamdayne.humify.payroll.repository;

import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    boolean existsByEmployeeIdAndStatusIn(Long employeeId, Collection<PayslipStatus> statuses);
}
