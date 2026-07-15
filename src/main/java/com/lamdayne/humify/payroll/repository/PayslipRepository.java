package com.lamdayne.humify.payroll.repository;

import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> , JpaSpecificationExecutor<Payslip> {
    boolean existsByEmployeeIdAndStatusIn(Long employeeId, Collection<PayslipStatus> statuses);

    Optional<Payslip> findByPayrollPeriod_IdAndEmployee_Id(Long payrollPeriodId, Long employeeId);

    List<Payslip> findByPayrollPeriod_Id(Long payrollPeriodId);

    Page<Payslip> findByPayrollPeriod_Id(
            Long payrollPeriodId,
            Pageable pageable
    );

    @Query("""
SELECT p FROM Payslip p
WHERE p.employee.id = :employeeId
  AND p.status IN :visibleStatuses
  AND (:year IS NULL OR p.payrollPeriod.year = :year)
""")
    Page<Payslip> findMyPayslips(
            @Param("employeeId") Long employeeId,
            @Param("visibleStatuses") List<PayslipStatus> visibleStatuses,
            @Param("year") Integer year,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE Payslip p SET p.status = :newStatus WHERE p.payrollPeriod.id = :payrollPeriodId AND p.status = :currentStatus")
    int bulkUpdateStatusByPeriod(
            @Param("payrollPeriodId") Long payrollPeriodId,
            @Param("currentStatus") PayslipStatus currentStatus,
            @Param("newStatus") PayslipStatus newStatus
    );

    @Modifying
    @Query("""
            UPDATE Payslip p SET p.status = :newStatus, p.paymentDate = :paymentDate
            WHERE p.payrollPeriod.id = :payrollPeriodId AND p.status = :currentStatus
            """)
    int bulkMarkAsPaid(
            @Param("payrollPeriodId") Long payrollPeriodId,
            @Param("currentStatus") PayslipStatus currentStatus,
            @Param("newStatus") PayslipStatus newStatus,
            @Param("paymentDate") LocalDate paymentDate
    );
}
