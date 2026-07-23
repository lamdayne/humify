package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.WebSwipeRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceLogResponse;
import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.entity.AttendanceLog;
import com.lamdayne.humify.attendance.enums.AttendanceLogType;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.enums.AttendanceVerifyMethod;
import com.lamdayne.humify.attendance.enums.CheckedStatus;
import com.lamdayne.humify.attendance.mapper.AttendanceLogMapper;
import com.lamdayne.humify.attendance.repository.AttendanceLogRepository;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.attendance.service.AttendanceLogService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceLogServiceImpl implements AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyService companyService;
    private final AttendanceLogMapper attendanceLogMapper;

    @Override
    @Transactional
    public AttendanceLogResponse registerWebSwipe(String email, Long companyId, WebSwipeRequest request, String ipAddress, String userAgent) {

        Employee employee = employeeRepository.findByEmailAndCompanyId(email, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Company company = companyService.getCompanyById(companyId);

        LocalDate today = LocalDate.now();
        Instant now = Instant.now();

        // 1. Khởi tạo hoặc lấy bảng công tổng hợp ngày hôm nay
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today)
                .orElseGet(() -> Attendance.builder()
                        .company(company)
                        .employee(employee)
                        .workDate(today)
                        .status(AttendanceStatus.ABSENT)
                        .checkedStatus(CheckedStatus.NOT_CHECKED)
                        .build());

        // 2. Xử lý logic linh hoạt (Bao trọn các trường hợp quên quẹt thẻ)
        if (request.getLogType() == AttendanceLogType.CHECK_IN) {

            if (attendance.getCheckInTime() == null) {
                attendance.setCheckInTime(now);
                attendance.setStatus(AttendanceStatus.PRESENT);
            }

        } else if (request.getLogType() == AttendanceLogType.CHECK_OUT) {

            attendance.setCheckOutTime(now);

            if (attendance.getStatus() == AttendanceStatus.ABSENT) {
                attendance.setStatus(AttendanceStatus.PRESENT);
            }
        }

        attendance = attendanceRepository.save(attendance);

        // 3. Luôn lưu lại lịch sử bấm (Log thô)
        AttendanceLog log = AttendanceLog.builder()
                .attendance(attendance)
                .employee(employee)
                .timestamp(now)
                .logType(request.getLogType())
                .verifyMethod(AttendanceVerifyMethod.GPS)
                .ipAddress(ipAddress)
                .deviceInfo(userAgent)
                .build();

        return attendanceLogMapper.toResponse(attendanceLogRepository.save(log));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceLogResponse> getAllLogsForHr(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("timestamp").descending());

        Page<AttendanceLog> pageData = attendanceLogRepository.findAll(pageable);

        List<AttendanceLogResponse> content = pageData.getContent().stream()
                .map(attendanceLogMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<AttendanceLogResponse>builder()
                .pageNo(page)
                .pageSize(pageData.getSize())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .items(content)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceLogResponse> getMyLogs(String email, Long companyId, LocalDate startDate, LocalDate endDate) {

        Employee employee = employeeRepository.findByEmailAndCompanyId(email, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

        return attendanceLogRepository.findByEmployeeIdAndTimestampBetweenOrderByTimestampDesc(
                        employee.getId(), startInstant, endInstant)
                .stream()
                .map(attendanceLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}