package com.lamdayne.humify.employee.repository;

import com.lamdayne.humify.employee.entity.EmployeeContract;
import com.lamdayne.humify.employee.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeContractRepository extends JpaRepository<EmployeeContract, Long>, JpaSpecificationExecutor<EmployeeContract> {

    Optional<EmployeeContract> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndContractNumberAndDeletedAtIsNull(Long companyId, String contractNumber);

    /**
     * SỬA: Thêm cast(... as date) cho cả :endDate và :startDate
     */
    @Query("""
            SELECT COUNT(c) > 0 FROM EmployeeContract c 
            WHERE c.employee.id = :employeeId 
              AND c.status = :activeStatus 
              AND c.company.id = :companyId 
              AND c.deletedAt IS NULL 
              AND (cast(:endDate as date) IS NULL OR c.startDate <= cast(:endDate as date)) 
              AND (c.endDate IS NULL OR cast(:startDate as date) <= c.endDate)
            """)
    boolean hasOverlapContract(@Param("employeeId") Long employeeId,
                               @Param("companyId") Long companyId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("activeStatus") ContractStatus activeStatus);

    default boolean hasOverlapContract(Long employeeId, Long companyId, LocalDate startDate, LocalDate endDate) {
        return hasOverlapContract(employeeId, companyId, startDate, endDate, ContractStatus.ACTIVE);
    }

    /**
     * Dùng Specification lọc động để tránh lỗi Type Inference của PostgreSQL
     */
    default Page<EmployeeContract> findWithFilters(Long companyId, Long employeeId, ContractStatus status, Pageable pageable) {
        Specification<EmployeeContract> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (companyId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("company").get("id"), companyId));
            }

            predicates = cb.and(predicates, cb.isNull(root.get("deletedAt")));

            if (employeeId != null) {
                predicates = cb.and(predicates, cb.equal(root.get("employee").get("id"), employeeId));
            }

            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            return predicates;
        };

        return findAll(spec, pageable);
    }


    @Query("""
            SELECT c FROM EmployeeContract c
            WHERE c.status = :status
              AND c.startDate <= :periodEndDate
              AND (c.endDate IS NULL OR c.endDate >= :periodStartDate)
            """)
    List<EmployeeContract> findActiveContractsOverlappingPeriod(
            @Param("status") ContractStatus status,
            @Param("periodStartDate") LocalDate periodStartDate,
            @Param("periodEndDate") LocalDate periodEndDate
    );
}