package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    @Query("""
        SELECT COUNT(lr) > 0
        FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
            AND lr.status IN ('PENDING', 'APPROVED')
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
    """)
    boolean existsOverlapRequest(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(lr) > 0
        FROM LeaveRequest lr
        WHERE lr.employee.id = :employeeId
            AND lr.status IN ('PENDING', 'APPROVED')
            AND lr.startDate <= :endDate
            AND lr.endDate >= :startDate
            AND lr.id != :requestId
    """)
    boolean existsOverlapRequestExcludingId(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("requestId") Long requestId
    );

}
