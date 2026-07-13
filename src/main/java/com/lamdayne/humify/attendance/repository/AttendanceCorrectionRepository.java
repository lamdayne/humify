package com.lamdayne.humify.attendance.repository;

import com.lamdayne.humify.attendance.entity.AttendanceCorrection;
import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceCorrectionRepository extends JpaRepository<AttendanceCorrection, Long> {

    // Kiểm tra trùng lặp: xem có đơn PENDING nào cho attendanceId này chưa
    boolean existsByAttendanceIdAndStatus(Long attendanceId, AttendanceCorrectionStatus status);

    // Tìm kiếm phân trang dành cho Quản lý / HR
    @Query("SELECT ac FROM AttendanceCorrection ac WHERE " +
            "(:status IS NULL OR ac.status = :status) AND " +
            "(:employeeId IS NULL OR ac.employee.id = :employeeId)")
    Page<AttendanceCorrection> findAllByFilters(
            @Param("status") AttendanceCorrectionStatus status,
            @Param("employeeId") Long employeeId,
            Pageable pageable);

    // Tìm kiếm đơn cá nhân của nhân viên
    List<AttendanceCorrection> findByEmployeeIdAndStatus(Long employeeId, AttendanceCorrectionStatus status);

    List<AttendanceCorrection> findByEmployeeId(Long employeeId);
}