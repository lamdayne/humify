package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.UpdateAttendanceManualRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceDetailResponse;
import com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse;
import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.entity.WorkShift;
import com.lamdayne.humify.attendance.enums.CheckedStatus;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.attendance.repository.AttendanceSpecification;
import com.lamdayne.humify.attendance.repository.WorkShiftRepository;
import com.lamdayne.humify.attendance.service.AttendanceService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.search.SearchCriteriaParser;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSpecification attendanceSpecification;
    private final WorkShiftRepository workShiftRepository;
    private final UserService userService;

    @Override
    public PageResponse<AttendanceDetailResponse> getHRView(Pageable pageable, String[] searchParams) {
        List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(searchParams != null ? searchParams : new String[0]);
        Specification<Attendance> searchSpec = attendanceSpecification.build(criteriaList);

        Page<Attendance> page = attendanceRepository.findAll(searchSpec, pageable);

        return PageResponse.<AttendanceDetailResponse>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(page.stream().map(this::mapToResponse).toList())
                .build();
    }

    @Override
    public List<AttendanceDetailResponse> getPersonalView(Long userId, String[] searchParams) {
        User user = userService.getUserById(userId);

        if (user.getEmployee() == null) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        Long currentEmployeeId = user.getEmployee().getId();

        List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(searchParams);
        Specification<Attendance> searchSpec = attendanceSpecification.build(criteriaList);
        Specification<Attendance> employeeSpec = (root, query, cb) ->
                cb.equal(root.get("employee").get("id"), currentEmployeeId);
        Specification<Attendance> finalSpec = employeeSpec.and(searchSpec);

        return attendanceRepository.findAll(finalSpec).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<AttendanceSummaryReportResponse> getSummaryReport(LocalDate start, LocalDate end) {
        if (start == null) {
            throw new AppException(ErrorCode.ATTENDANCE_START_DATE_REQUIRED);
        }
        if (end == null) {
            throw new AppException(ErrorCode.ATTENDANCE_END_DATE_REQUIRED);
        }
        if (start.isAfter(end)) {
            throw new AppException(ErrorCode.INVALID_FILTER_VALUE);
        }

        return attendanceRepository.getSummaryReport(start, end);
    }

    @Override
    @Transactional
    public AttendanceDetailResponse updateManual(Long id, Long currentUserId, UpdateAttendanceManualRequest request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND));

        User actor = userService.getUserById(currentUserId);

        attendance.setCheckInTime(request.getManualCheckIn());
        attendance.setCheckOutTime(request.getManualCheckOut());
        attendance.setWorkPoints(request.getWorkPoints());
        attendance.setStatus(AttendanceStatus.valueOf(request.getStatus()));
        attendance.setModificationReason(request.getModificationReason());
        attendance.setIsModified(Boolean.TRUE);
        attendance.setModifiedBy(actor);

        if (request.getWorkShiftId() != null) {
            WorkShift shift = workShiftRepository.findById(request.getWorkShiftId())
                    .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
            attendance.setWorkShift(shift);
        }

        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            long seconds = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toSeconds();
            BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
            attendance.setWorkedHours(hours);
            attendance.setCheckedStatus(CheckedStatus.CHECKED_OUT);
        } else if (attendance.getCheckInTime() != null) {
            attendance.setCheckedStatus(CheckedStatus.CHECKED_IN);
            attendance.setWorkedHours(BigDecimal.ZERO);
        } else {
            attendance.setCheckedStatus(CheckedStatus.NOT_CHECKED);
            attendance.setWorkedHours(BigDecimal.ZERO);
        }

        if (request.getOtHours() != null) {
            attendance.setOtHours(request.getOtHours());
        }

        attendance.setLateMinutes(0);
        attendance.setEarlyMinutes(0);

        return mapToResponse(attendanceRepository.save(attendance));
    }

    private AttendanceDetailResponse mapToResponse(Attendance a) {
        return AttendanceDetailResponse.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getFullName())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .workShiftId(a.getWorkShift() != null ? a.getWorkShift().getId() : null)
                .workShiftCode(a.getWorkShift() != null ? a.getWorkShift().getShiftCode() : null)
                .workDate(a.getWorkDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .workedHours(a.getWorkedHours())
                .lateMinutes(a.getLateMinutes())
                .earlyMinutes(a.getEarlyMinutes())
                .otHours(a.getOtHours())
                .workPoints(a.getWorkPoints())
                .checkedStatus(a.getCheckedStatus().name())
                .status(a.getStatus().name())
                .isModified(a.getIsModified())
                .build();
    }
}