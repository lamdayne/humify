package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.AttendanceApprovalRequest;
import com.lamdayne.humify.attendance.dto.request.AttendanceCorrectionRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceCorrectionResponse;
import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.entity.AttendanceCorrection;
import com.lamdayne.humify.attendance.enums.AttendanceCorrectionStatus;
import com.lamdayne.humify.attendance.mapper.AttendanceCorrectionMapper; // <-- ĐÃ THÊM DÒNG IMPORT NÀY
import com.lamdayne.humify.attendance.repository.AttendanceCorrectionRepository;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.attendance.service.AttendanceCorrectionService;
import com.lamdayne.humify.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceCorrectionServiceImpl implements AttendanceCorrectionService {

    private final AttendanceCorrectionRepository correctionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceCorrectionMapper correctionMapper; // Spring sẽ tự động inject Bean do MapStruct sinh ra vào đây

    @Override
    @Transactional
    public AttendanceCorrectionResponse createCorrection(AttendanceCorrectionRequest request, User currentUser) {
        Attendance attendance = attendanceRepository.findById(request.getAttendanceId())
                .orElseThrow(() -> new RuntimeException("ATTENDANCE_NOT_FOUND"));

        // Nghiệp vụ 1: Kiểm tra quyền sở hữu bản ghi công
        if (!attendance.getEmployee().getId().equals(currentUser.getEmployee().getId())) {
            throw new RuntimeException("ACCESS_DENIED");
        }


        // Nghiệp vụ 3: Kiểm tra trùng lặp đơn PENDING
        boolean existsPending = correctionRepository.existsByAttendanceIdAndStatus(
                request.getAttendanceId(), AttendanceCorrectionStatus.PENDING);
        if (existsPending) {
            throw new RuntimeException("PENDING_CORRECTION_ALREADY_EXISTS");
        }

        AttendanceCorrection correction = AttendanceCorrection.builder()
                .attendance(attendance)
                .employee(currentUser.getEmployee())
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
    public Page<AttendanceCorrectionResponse> getCorrectionsForManager(AttendanceCorrectionStatus status, Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return correctionRepository.findAllByFilters(status, employeeId, pageable).map(correctionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceCorrectionResponse> getMyCorrections(AttendanceCorrectionStatus status, User currentUser) {
        Long empId = currentUser.getEmployee().getId();
        List<AttendanceCorrection> list = (status != null)
                ? correctionRepository.findByEmployeeIdAndStatus(empId, status)
                : correctionRepository.findByEmployeeId(empId);

        return list.stream().map(correctionMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceCorrectionResponse approveCorrection(Long id, AttendanceApprovalRequest request, User currentUser) {
        AttendanceCorrection correction = correctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CORRECTION_NOT_FOUND"));

        if (correction.getStatus() != AttendanceCorrectionStatus.PENDING) {
            throw new RuntimeException("CORRECTION_ALREADY_PROCESSED");
        }

        correction.setStatus(AttendanceCorrectionStatus.APPROVED);
        correction.setApprover(currentUser);
        correction.setApprovedAt(Instant.now());
        correction.setApproverNote(request.getApproverNote());

        Attendance attendance = correction.getAttendance();
        if (correction.getRequestedCheckIn() != null) {
            attendance.setCheckInTime(correction.getRequestedCheckIn());
        }
        if (correction.getRequestedCheckOut() != null) {
            attendance.setCheckOutTime(correction.getRequestedCheckOut());
        }
        attendance.setIsModified(true);
        attendance.setModifiedBy(currentUser);
        attendance.setModificationReason("[Giải trình được duyệt] - Lý do của nhân viên: " + correction.getReason());



        attendanceRepository.save(attendance);
        return correctionMapper.toResponse(correctionRepository.save(correction));
    }

    @Override
    @Transactional
    public AttendanceCorrectionResponse rejectCorrection(Long id, AttendanceApprovalRequest request, User currentUser) {
        AttendanceCorrection correction = correctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CORRECTION_NOT_FOUND"));

        if (correction.getStatus() != AttendanceCorrectionStatus.PENDING) {
            throw new RuntimeException("CORRECTION_ALREADY_PROCESSED");
        }

        correction.setStatus(AttendanceCorrectionStatus.REJECTED);
        correction.setApprover(currentUser);
        correction.setApprovedAt(Instant.now());
        correction.setApproverNote(request.getApproverNote());

        return correctionMapper.toResponse(correctionRepository.save(correction));
    }
}