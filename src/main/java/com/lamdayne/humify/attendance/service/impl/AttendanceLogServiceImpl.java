package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.NfcSwipeRequest;
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
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceLogServiceImpl implements AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
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
    @Transactional
    public AttendanceLogResponse registerNfcSwipe(NfcSwipeRequest request) {
        String companyCode = request.getCompanyCode() != null ? request.getCompanyCode().trim() : "";
        String employeeCode = request.getEmployeeCode() != null ? request.getEmployeeCode().trim() : "";
        String cardUid = request.getCardUid() != null ? request.getCardUid().trim() : "";

        log.info("Processing NFC Swipe: employeeCode='{}', companyCode='{}', cardUid='{}'", employeeCode, companyCode, cardUid);

        CompanyContext.setAdmin(true);
        try {
            Employee employee = null;

            if (!companyCode.isEmpty()) {
                Optional<Company> companyOpt = companyRepository.findByCompanyCode(companyCode);
                if (companyOpt.isPresent()) {
                    Long companyId = companyOpt.get().getId();
                    if (!cardUid.isEmpty()) {
                        employee = employeeRepository.findAllByNfcCardUidIgnoreCase(cardUid).stream()
                                .filter(e -> e.getCompany().getId().equals(companyId)
                                        && e.getDeletedAt() == null
                                        && e.getStatus() != EmployeeStatus.RESIGNED
                                        && e.getStatus() != EmployeeStatus.TERMINATED)
                                .findFirst()
                                .orElse(null);
                    }
                    if (employee == null && !employeeCode.isEmpty()) {
                        employee = employeeRepository.findByEmployeeCodeAndCompanyId(employeeCode, companyId)
                                .filter(e -> e.getDeletedAt() == null
                                        && e.getStatus() != EmployeeStatus.RESIGNED
                                        && e.getStatus() != EmployeeStatus.TERMINATED)
                                .orElse(null);
                    }
                }
            }

            if (employee == null && !cardUid.isEmpty()) {
                employee = employeeRepository.findAllByNfcCardUidIgnoreCase(cardUid).stream()
                        .filter(e -> e.getDeletedAt() == null
                                && e.getStatus() != EmployeeStatus.RESIGNED
                                && e.getStatus() != EmployeeStatus.TERMINATED)
                        .findFirst()
                        .orElse(null);
            }

            if (employee == null && !employeeCode.isEmpty()) {
                employee = employeeRepository.findAllByEmployeeCodeIgnoreCase(employeeCode).stream()
                        .filter(e -> e.getDeletedAt() == null
                                && e.getStatus() != EmployeeStatus.RESIGNED
                                && e.getStatus() != EmployeeStatus.TERMINATED)
                        .findFirst()
                        .orElse(null);
            }

            if (employee == null) {
                log.warn("NFC Swipe failed: No active employee found for employeeCode='{}', cardUid='{}'", employeeCode, cardUid);
                throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
            }

            log.info("Found employee for NFC Swipe: id={}, employeeCode={}, fullName={}", employee.getId(), employee.getEmployeeCode(), employee.getFullName());

            if ((employee.getNfcCardUid() == null || employee.getNfcCardUid().isBlank()) && !cardUid.isEmpty()) {
                final Long currentEmpId = employee.getId();
                boolean cardInUseByOtherActiveEmp = employeeRepository.findAllByNfcCardUidIgnoreCase(cardUid).stream()
                        .anyMatch(e -> !e.getId().equals(currentEmpId)
                                && e.getDeletedAt() == null
                                && e.getStatus() != EmployeeStatus.RESIGNED
                                && e.getStatus() != EmployeeStatus.TERMINATED);

                if (cardInUseByOtherActiveEmp) {
                    log.warn("Cannot auto-bind cardUid='{}' to employeeId={}: card is already in use by another active employee", cardUid, currentEmpId);
                    throw new AppException(ErrorCode.NFC_CARD_ALREADY_EXISTS);
                }

                employee.setNfcCardUid(cardUid);
                employee = employeeRepository.save(employee);
                log.info("Automatically linked cardUid='{}' to employeeId={}", cardUid, employee.getId());
            }

            Company company = employee.getCompany();
            CompanyContext.setCompanyId(company.getId());

            LocalDate today = LocalDate.now();
            Instant now = Instant.now();

            final Employee finalEmployee = employee;
            final Company finalCompany = company;

            Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(finalEmployee.getId(), today)
                    .orElseGet(() -> Attendance.builder()
                            .company(finalCompany)
                            .employee(finalEmployee)
                            .workDate(today)
                            .status(AttendanceStatus.ABSENT)
                            .checkedStatus(CheckedStatus.NOT_CHECKED)
                            .build());

            AttendanceLogType logType;
            if (attendance.getCheckInTime() == null) {
                logType = AttendanceLogType.CHECK_IN;
                attendance.setCheckInTime(now);
                attendance.setStatus(AttendanceStatus.PRESENT);
                attendance.setCheckedStatus(CheckedStatus.CHECKED_IN);
            } else {
                logType = AttendanceLogType.CHECK_OUT;
                attendance.setCheckOutTime(now);
                attendance.setCheckedStatus(CheckedStatus.CHECKED_OUT);

                long seconds = Duration.between(attendance.getCheckInTime(), now).toSeconds();
                BigDecimal hours = BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
                attendance.setWorkedHours(hours);
            }

            attendance = attendanceRepository.save(attendance);

            AttendanceLog log = AttendanceLog.builder()
                    .attendance(attendance)
                    .employee(finalEmployee)
                    .timestamp(now)
                    .logType(logType)
                    .verifyMethod(AttendanceVerifyMethod.NFC)
                    .ipAddress(null)
                    .deviceInfo(request.getDeviceInfo() != null ? request.getDeviceInfo() : "ESP32 NFC Reader")
                    .build();

            return attendanceLogMapper.toResponse(attendanceLogRepository.save(log));
        } finally {
            CompanyContext.clear();
        }
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