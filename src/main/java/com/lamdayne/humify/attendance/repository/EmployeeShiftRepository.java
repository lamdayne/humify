package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.EmployeeShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long>, JpaSpecificationExecutor<EmployeeShift> {

    @Query("SELECT COUNT(es) > 0 FROM EmployeeShift es WHERE es.employee.id = :employeeId " +
           "AND es.deletedAt IS NULL " +
           "AND (cast(:id as long) IS NULL OR es.id <> :id) " +
           "AND (es.endDate IS NULL OR cast(:startDate as date) <= es.endDate) " +
           "AND (cast(:endDate as date) IS NULL OR cast(:endDate as date) >= es.startDate)")
    boolean hasOverlap(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("id") Long id
    );

    @Query("SELECT es FROM EmployeeShift es " +
           "WHERE es.employee.id = :employeeId " +
           "AND es.deletedAt IS NULL " +
           "AND es.startDate <= :workDate " +
           "AND (es.endDate IS NULL OR es.endDate >= :workDate) " +
           "ORDER BY es.id DESC")
    List<EmployeeShift> findActiveShiftsByEmployeeIdAndDate(
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate
    );
}
