package com.lamdayne.humify.performance.repository;

import com.lamdayne.humify.performance.entity.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Long> {

    Optional<Kpi> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT k FROM Kpi k WHERE k.employee.id = :employeeId AND k.company.id = :companyId " +
            "AND (:startDate IS NULL OR k.startDate >= :startDate) " +
            "AND (:endDate IS NULL OR k.endDate <= :endDate)")
    List<Kpi> findAllByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(k.weight), 0.0) FROM Kpi k " +
            "WHERE k.employee.id = :employeeId AND k.company.id = :companyId AND k.status = 'IN_PROGRESS' " +
            "AND (:kpiId IS NULL OR k.id != :kpiId)")
    Double sumWeightByEmployeeIdAndCompanyId(
            @Param("employeeId") Long employeeId,
            @Param("companyId") Long companyId,
            @Param("kpiId") Long kpiId);
}