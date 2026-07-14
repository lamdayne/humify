package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    List<AttendanceLog> findByEmployeeIdAndTimestampBetweenOrderByTimestampDesc(Long employeeId, Instant start, Instant end);
}
