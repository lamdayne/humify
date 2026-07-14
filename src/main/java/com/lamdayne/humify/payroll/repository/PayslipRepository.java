package com.lamdayne.humify.payroll.repository;

import com.lamdayne.humify.payroll.entity.Payslip;
import com.lamdayne.humify.payroll.enums.PayslipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    boolean existsByEmployeeIdAndStatusIn(Long employeeId, Collection<PayslipStatus> statuses);


    Optional<Payslip> findByIdAndCompany_Id(Long id, Long companyId);

    Optional<Payslip> findByPayrollPeriod_IdAndEmployee_Id(Long payrollPeriodId, Long employeeId);

    List<Payslip> findByPayrollPeriod_Id(Long payrollPeriodId);

    Page<Payslip> findByCompany_IdAndPayrollPeriod_Id(
            Long companyId,
            Long payrollPeriodId,
            Pageable pageable
    );

    Page<Payslip> findByCompany_IdAndPayrollPeriod_IdAndStatus(
            Long companyId,
            Long payrollPeriodId,
            PayslipStatus status,
            Pageable pageable
    );

    Page<Payslip> findByCompany_IdAndPayrollPeriod_IdAndEmployee_Id(
            Long companyId,
            Long payrollPeriodId,
            Long employeeId,
            Pageable pageable
    );

    Page<Payslip> findByCompany_IdAndPayrollPeriod_IdAndEmployee_IdAndStatus(
            Long companyId,
            Long payrollPeriodId,
            Long employeeId,
            PayslipStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM Payslip p
            WHERE p.company.id = :companyId
              AND p.employee.id = :employeeId
              AND p.status IN :visibleStatuses
              AND (:year IS NULL OR p.payrollPeriod.year = :year)
            """)
    Page<Payslip> findMyPayslips(
            @Param("companyId") Long companyId,
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
