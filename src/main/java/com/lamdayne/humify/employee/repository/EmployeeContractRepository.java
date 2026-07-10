package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeContract;
import com.lamdayne.humify.employee.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long> {

    Optional<EmployeeContract> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndContractNumberAndDeletedAtIsNull(Long companyId, String contractNumber);

    @Query("SELECT COUNT(c) > 0 FROM EmployeeContract c WHERE c.employee.id = :employeeId " +
            "AND c.status = 'ACTIVE' AND c.company.id = :companyId AND c.deletedAt IS NULL " +
            "AND (:endDate IS NULL OR c.startDate <= :endDate) " +
            "AND (c.endDate IS NULL OR :startDate <= c.endDate)")
    boolean hasOverlapContract(@Param("employeeId") Long employeeId,
                               @Param("companyId") Long companyId,
                               @Param("startDate") java.time.LocalDate startDate,
                               @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT c FROM EmployeeContract c WHERE c.company.id = :companyId AND c.deletedAt IS NULL " +
            "AND (CAST(:employeeId AS Long) IS NULL OR c.employee.id = :employeeId) " +
            "AND (CAST(:status AS String) IS NULL OR c.status = :status)")
    Page<EmployeeContract> findWithFilters(@Param("companyId") Long companyId,
                                           @Param("employeeId") Long employeeId,
                                           @Param("status") ContractStatus status,
                                           Pageable pageable);
}