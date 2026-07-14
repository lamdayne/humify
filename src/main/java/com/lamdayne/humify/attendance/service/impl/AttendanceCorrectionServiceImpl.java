package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.AttendanceCorrectionRequest;
import com.lamdayne.humify.attendance.dto.request.CorrectionActionRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.entity.AttendanceCorrection;
import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import com.lamdayne.humify.attendance.mapper.AttendanceCorrectionMapper;
import com.lamdayne.humify.attendance.repository.AttendanceCorrectionRepository;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.attendance.service.AttendanceCorrectionService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceCorrectionServiceImpl implements AttendanceCorrectionService {

    private final AttendanceCorrectionRepository correctionRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;
    private final UserService userService;
    private final AttendanceCorrectionMapper correctionMapper;

    @Override
    @Transactional
    public AttendanceCorrectionResponse createCorrection(String email, AttendanceCorrectionRequest request) {
        // Tìm trực tiếp từ EmployeeRepository bằng email
        Employee employee = employeeService.getEmployeeByEmail(email);

        Attendance attendance = attendanceRepository.findById(request.getAttendanceId())
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_NOT_FOUND));

        // Kiểm tra quyền sở hữu
        if (!attendance.getEmployee().getId().equals(employee.getId())) {
            throw new AppException(ErrorCode.ATTENDANCE_ACCESS_DENIED);
        }

        // Kiểm tra kỳ công (Payroll Period)
        checkPayrollPeriod(attendance.getWorkDate());

        // Kiểm tra trùng lặp đơn PENDING
        if (correctionRepository.existsByAttendanceIdAndStatus(attendance.getId(), AttendanceCorrectionStatus.PENDING)) {
            throw new AppException(ErrorCode.PENDING_CORRECTION_ALREADY_EXISTS);
        }

        AttendanceCorrection correction = AttendanceCorrection.builder()
                .attendance(attendance)
                .employee(employee)
                .correctionDate(attendance.getWorkDate())
                .requestedCheckIn(request.getRequestedCheckIn())
                .requestedCheckOut(request.getRequestedCheckOut())
                .reason(request.getReason())
                .status(AttendanceCorrectionStatus.PENDING)
                .build();

        return correctionMapper.toResponse(correctionRepository.save(correction));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceCorrectionResponse> getMyCorrections(String email, String status, int page, int size) {
        // Tìm trực tiếp từ EmployeeRepository
        Employee employee = employeeService.getEmployeeByEmail(email);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<AttendanceCorrection> pageData;

        if (status != null && !status.isBlank()) {
            pageData = correctionRepository.findByEmployeeIdAndStatus(employee.getId(), AttendanceCorrectionStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            pageData = correctionRepository.findByEmployeeId(employee.getId(), pageable);
        }

        return buildPageResponse(pageData, page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceCorrectionResponse> getAllCorrectionsForHr(String status, Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<AttendanceCorrection> pageData = correctionRepository.findAll(pageable);

        return buildPageResponse(pageData, page);
    }

    @Override
    @Transactional
    public AttendanceCorrectionResponse approveCorrection(Long correctionId, Long approverUserId, CorrectionActionRequest request) {
        AttendanceCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new AppException(ErrorCode.CORRECTION_NOT_FOUND));

        if (correction.getStatus() != AttendanceCorrectionStatus.PENDING) {
            throw new AppException(ErrorCode.CORRECTION_ALREADY_PROCESSED);
        }

        // Tìm thông qua UserService
        User approver = userService.getUserById(approverUserId);

        correction.setStatus(AttendanceCorrectionStatus.APPROVED);
        correction.setApprover(approver);
        correction.setApprovedAt(Instant.now());
        correction.setApproverNote(request.getApproverNote());
        correctionRepository.save(correction);

        Attendance attendance = correction.getAttendance();
        if (correction.getRequestedCheckIn() != null) {
            attendance.setCheckInTime(correction.getRequestedCheckIn());
        }
        if (correction.getRequestedCheckOut() != null) {
            attendance.setCheckOutTime(correction.getRequestedCheckOut());
        }
        attendance.setIsModified(true);
        attendance.setModifiedBy(approver);
        attendance.setModificationReason(correction.getReason());

        BigDecimal workedHours = recalculateAttendanceData(attendance);
        attendance.setWorkedHours(workedHours);
        attendanceRepository.save(attendance);

        return correctionMapper.toResponse(correction);
    }

    @Override
    @Transactional
    public AttendanceCorrectionResponse rejectCorrection(Long correctionId, Long approverUserId, CorrectionActionRequest request) {
        AttendanceCorrection correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new AppException(ErrorCode.CORRECTION_NOT_FOUND));

        if (correction.getStatus() != AttendanceCorrectionStatus.PENDING) {
            throw new AppException(ErrorCode.CORRECTION_ALREADY_PROCESSED);
        }

        // Tìm thông qua UserService
        User approver = userService.getUserById(approverUserId);

        correction.setStatus(AttendanceCorrectionStatus.REJECTED);
        correction.setApprover(approver);
        correction.setApprovedAt(Instant.now());
        correction.setApproverNote(request.getApproverNote());

        return correctionMapper.toResponse(correctionRepository.save(correction));
    }

    // --- CÁC HÀM XỬ LÝ NỘI BỘ (PRIVATE METHODS) ---

    private void checkPayrollPeriod(LocalDate workDate) {
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        if (workDate.isBefore(firstDayOfCurrentMonth)) {
            throw new AppException(ErrorCode.PAYROLL_PERIOD_CLOSED);
        }
    }

    private BigDecimal recalculateAttendanceData(Attendance attendance) {
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            long minutes = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
            if (minutes > 0) {
                 return BigDecimal.valueOf(minutes / 60);
            }
        }
        return attendance.getWorkedHours();
    }

    private PageResponse<AttendanceCorrectionResponse> buildPageResponse(Page<AttendanceCorrection> pageData, int page) {
        List<AttendanceCorrectionResponse> content = pageData.getContent().stream()
                .map(correctionMapper::toResponse)
                .toList();

        return PageResponse.<AttendanceCorrectionResponse>builder()
                .pageNo(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .items(content)
                .build();
    }
}