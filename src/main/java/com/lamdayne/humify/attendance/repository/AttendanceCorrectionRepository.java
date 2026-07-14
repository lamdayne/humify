package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.AttendanceCorrection;
import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, Long> {

    // Kiểm tra xem đã có đơn PENDING nào cho bản công này chưa
    boolean existsByAttendanceIdAndStatus(Long attendanceId, AttendanceCorrectionStatus status);

    // Dành cho API lấy đơn cá nhân
    Page<AttendanceCorrection> findByEmployeeIdAndStatus(Long employeeId, AttendanceCorrectionStatus status, Pageable pageable);

    Page<AttendanceCorrection> findByEmployeeId(Long employeeId, Pageable pageable);
}
