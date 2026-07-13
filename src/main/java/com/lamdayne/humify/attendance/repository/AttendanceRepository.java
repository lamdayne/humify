package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    @Query("SELECT new com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse(" +
            "a.employee.id, a.employee.fullName, " +
            "SUM(CASE WHEN a.status IN ('PRESENT', 'LATE', 'REMOTE') THEN 1.0 WHEN a.status = 'HALF_DAY' THEN 0.5 ELSE 0.0 END), " +
            "SUM(a.workedHours), " +
            "SUM(CASE WHEN a.lateMinutes > 0 THEN 1L ELSE 0L END), " +
            "SUM(CAST(a.lateMinutes AS Long)), " +
            "SUM(CASE WHEN a.earlyMinutes > 0 THEN 1L ELSE 0L END), " +
            "SUM(a.otHours), " +
            "SUM(CASE WHEN a.status = 'LEAVE' THEN 1.0 ELSE 0.0 END), " +
            "SUM(CASE WHEN a.status = 'ABSENT' THEN 1.0 ELSE 0.0 END)) " +
            "FROM Attendance a WHERE a.workDate BETWEEN :start AND :end " +
            "GROUP BY a.employee.id, a.employee.fullName")
    List<AttendanceSummaryReportResponse> getSummaryReport(@Param("start") LocalDate start,
                                                           @Param("end") LocalDate end);
}