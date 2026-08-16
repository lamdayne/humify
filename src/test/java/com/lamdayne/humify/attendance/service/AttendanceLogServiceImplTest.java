package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.NfcSwipeRequest;
import com.lamdayne.humify.attendance.dto.request.WebSwipeRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceLogResponse;
import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.entity.AttendanceLog;
import com.lamdayne.humify.attendance.enums.AttendanceLogType;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.enums.CheckedStatus;
import com.lamdayne.humify.attendance.mapper.AttendanceLogMapper;
import com.lamdayne.humify.attendance.repository.AttendanceLogRepository;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.attendance.service.impl.AttendanceLogServiceImpl;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceLogServiceImpl Tests")
class AttendanceLogServiceImplTest {

    @Mock private AttendanceLogRepository attendanceLogRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyService companyService;
    @Mock private AttendanceLogMapper attendanceLogMapper;

    @InjectMocks
    private AttendanceLogServiceImpl attendanceLogService;

    private Employee employee;
    private Company company;
    private AttendanceLog attendanceLog;
    private AttendanceLogResponse attendanceLogResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceLogService, "expectedIotApiKey", "IoT-Default-Auth-Key-2026");

        company = Company.builder().build();
        ReflectionTestUtils.setField(company, "id", 1L);

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", 1L);
        employee.setCompany(company);
        employee.setEmail("nv@company.com");
        employee.setStatus(EmployeeStatus.ACTIVE);

        attendanceLog = AttendanceLog.builder().build();
        attendanceLogResponse = AttendanceLogResponse.builder().build();
    }

    // ---- registerWebSwipe ----

    @Test
    @DisplayName("Register web swipe check-in when no attendance exists")
    void registerWebSwipe_checkIn_newAttendance() {
        WebSwipeRequest request = new WebSwipeRequest();
        request.setLogType(AttendanceLogType.CHECK_IN);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .build();

        when(employeeRepository.findByEmailAndCompanyId("nv@company.com", 1L))
                .thenReturn(Optional.of(employee));
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceLogRepository.save(any(AttendanceLog.class))).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerWebSwipe("nv@company.com", 1L, request, "127.0.0.1", "Chrome");

        assertThat(result).isNotNull();
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Register web swipe check-in when attendance exists but not checked-in")
    void registerWebSwipe_checkIn_existingAttendanceNoCheckIn() {
        WebSwipeRequest request = new WebSwipeRequest();
        request.setLogType(AttendanceLogType.CHECK_IN);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .build();
        // checkInTime = null -> se duoc set

        when(employeeRepository.findByEmailAndCompanyId("nv@company.com", 1L)).thenReturn(Optional.of(employee));
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerWebSwipe("nv@company.com", 1L, request, "127.0.0.1", "Chrome");

        assertThat(result).isNotNull();
        assertThat(attendance.getCheckInTime()).isNotNull();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Register web swipe check-in ignored when already checked-in")
    void registerWebSwipe_checkIn_alreadyCheckedIn() {
        WebSwipeRequest request = new WebSwipeRequest();
        request.setLogType(AttendanceLogType.CHECK_IN);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .checkedStatus(CheckedStatus.CHECKED_IN)
                .build();
        attendance.setCheckInTime(Instant.now()); // da check in

        when(employeeRepository.findByEmailAndCompanyId("nv@company.com", 1L)).thenReturn(Optional.of(employee));
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        attendanceLogService.registerWebSwipe("nv@company.com", 1L, request, "127.0.0.1", "Chrome");

        // checkInTime khong bi thay doi
        verify(attendanceRepository).save(attendance);
    }

    @Test
    @DisplayName("Register web swipe check-out when attendance status is absent")
    void registerWebSwipe_checkOut_whenAbsent() {
        WebSwipeRequest request = new WebSwipeRequest();
        request.setLogType(AttendanceLogType.CHECK_OUT);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .build();

        when(employeeRepository.findByEmailAndCompanyId("nv@company.com", 1L)).thenReturn(Optional.of(employee));
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        attendanceLogService.registerWebSwipe("nv@company.com", 1L, request, "127.0.0.1", "Chrome");

        assertThat(attendance.getCheckOutTime()).isNotNull();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Register web swipe check-out when attendance status is present")
    void registerWebSwipe_checkOut_whenPresent() {
        WebSwipeRequest request = new WebSwipeRequest();
        request.setLogType(AttendanceLogType.CHECK_OUT);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .checkedStatus(CheckedStatus.CHECKED_IN)
                .build();
        attendance.setCheckInTime(Instant.now());

        when(employeeRepository.findByEmailAndCompanyId("nv@company.com", 1L)).thenReturn(Optional.of(employee));
        when(companyService.getCompanyById(1L)).thenReturn(company);
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        attendanceLogService.registerWebSwipe("nv@company.com", 1L, request, "127.0.0.1", "Chrome");

        assertThat(attendance.getCheckOutTime()).isNotNull();
        // Status khong doi vi khong phai ABSENT
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Register web swipe throws exception when employee not found")
    void registerWebSwipe_throwsWhenEmployeeNotFound() {
        WebSwipeRequest request = new WebSwipeRequest();
        request.setLogType(AttendanceLogType.CHECK_IN);

        when(employeeRepository.findByEmailAndCompanyId("unknown@company.com", 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceLogService.registerWebSwipe("unknown@company.com", 1L, request, "127.0.0.1", "Chrome"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    // ---- registerNfcSwipe ----

    @Test
    @DisplayName("Register nfc swipe throws exception when API key mismatches")
    void registerNfcSwipe_throwsWhenApiKeyMismatch() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("C001");
        request.setEmployeeCode("EMP001");
        request.setCardUid("CARD123");

        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "WRONG_KEY"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.UNAUTHENTICATED));
    }

    @Test
    @DisplayName("Register nfc swipe check-in successfully with company code and card UID")
    void registerNfcSwipe_checkIn_byCompanyCodeAndCardUid() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("C001");
        request.setCardUid("CARD123");
        request.setDeviceInfo("ESP32");

        Company foundCompany = Company.builder().build();
        ReflectionTestUtils.setField(foundCompany, "id", 1L);
        employee.setNfcCardUid("CARD123");
        employee.setStatus(EmployeeStatus.ACTIVE);

        when(companyRepository.findByCompanyCode("C001")).thenReturn(Optional.of(foundCompany));
        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD123")).thenReturn(List.of(employee));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(foundCompany)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Register nfc swipe check-out successfully when already checked-in")
    void registerNfcSwipe_checkOut_whenAlreadyCheckedIn() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCardUid("CARD123");
        request.setCompanyCode("");

        employee.setNfcCardUid("CARD123");
        employee.setStatus(EmployeeStatus.ACTIVE);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .checkedStatus(CheckedStatus.CHECKED_IN)
                .build();
        attendance.setCheckInTime(Instant.now().minusSeconds(3600));

        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD123")).thenReturn(List.of(employee));
        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");

        assertThat(result).isNotNull();
        assertThat(attendance.getCheckOutTime()).isNotNull();
    }

    @Test
    @DisplayName("Register nfc swipe throws exception when employee not found")
    void registerNfcSwipe_throwsWhenEmployeeNotFound() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("");
        request.setCardUid("");
        request.setEmployeeCode("");

        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Test
    @DisplayName("Register nfc swipe successfully by employee code fallback")
    void registerNfcSwipe_byEmployeeCodeFallback() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("");
        request.setCardUid("");
        request.setEmployeeCode("EMP001");

        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setNfcCardUid("EXISTING_CARD");

        when(employeeRepository.findAllByEmployeeCodeIgnoreCase("EMP001")).thenReturn(List.of(employee));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(1L), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Register nfc swipe automatically binds card UID to employee")
    void registerNfcSwipe_autoBindNfcCard() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("");
        request.setCardUid("NEW_CARD_UID");
        request.setEmployeeCode("");

        employee.setNfcCardUid(null); // Chua co card

        Employee updatedEmployee = new Employee();
        ReflectionTestUtils.setField(updatedEmployee, "id", 1L);
        updatedEmployee.setCompany(company);
        updatedEmployee.setStatus(EmployeeStatus.ACTIVE);
        updatedEmployee.setNfcCardUid("NEW_CARD_UID");

        when(employeeRepository.findAllByNfcCardUidIgnoreCase("NEW_CARD_UID")).thenReturn(List.of(employee));
        // Card chua duoc dung boi nhan vien nao khac
        when(employeeRepository.save(any())).thenReturn(updatedEmployee);

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .company(company)
                .workDate(LocalDate.now())
                .status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(anyLong(), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");

        assertThat(result).isNotNull();
        verify(employeeRepository).save(any()); // da luu NFC card
    }

    @Test
    @DisplayName("Register nfc swipe throws exception when card is already in use")
    void registerNfcSwipe_throwsWhenCardInUseByOtherEmployee() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("");
        request.setCardUid("SHARED_CARD");
        request.setEmployeeCode("");

        employee.setNfcCardUid(null);

        Employee otherEmployee = new Employee();
        ReflectionTestUtils.setField(otherEmployee, "id", 2L);
        otherEmployee.setStatus(EmployeeStatus.ACTIVE);

        // findAll tra ve employee hien tai (chua co card)
        when(employeeRepository.findAllByNfcCardUidIgnoreCase("SHARED_CARD"))
                .thenReturn(List.of(employee)) // lan 1 tim employee
                .thenReturn(List.of(otherEmployee)); // lan 2 kiem tra card trong dung

        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NFC_CARD_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("Register nfc swipe successfully when API key is null")
    void registerNfcSwipe_nullApiKeyAccepted() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("");
        request.setCardUid("CARD001");
        request.setEmployeeCode("");

        employee.setNfcCardUid("CARD001");
        employee.setStatus(EmployeeStatus.ACTIVE);

        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD001")).thenReturn(List.of(employee));

        Attendance attendance = Attendance.builder()
                .employee(employee).company(company)
                .workDate(LocalDate.now()).status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED).build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(anyLong(), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(any())).thenReturn(attendanceLogResponse);

        // null API key -> skip key check
        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, null);

        assertThat(result).isNotNull();
    }

    // ---- getAllLogsForHr ----

    @Test
    @DisplayName("Get all logs for HR returns valid PageResponse")
    void getAllLogsForHr_success() {
        Page<AttendanceLog> page = new PageImpl<>(List.of(attendanceLog));
        when(attendanceLogRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        PageResponse<AttendanceLogResponse> result = attendanceLogService.getAllLogsForHr(null, 1, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getPageNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get all logs for HR returns empty list")
    void getAllLogsForHr_empty() {
        Page<AttendanceLog> page = new PageImpl<>(Collections.emptyList());
        when(attendanceLogRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<AttendanceLogResponse> result = attendanceLogService.getAllLogsForHr(null, 1, 10);

        assertThat(result.getItems()).isEmpty();
    }

    // ---- getMyLogs ----

    @Test
    @DisplayName("Get my logs returns valid list of logs")
    void getMyLogs_success() {
        when(employeeRepository.findByEmailAndCompanyId("nv@company.com", 1L)).thenReturn(Optional.of(employee));
        when(attendanceLogRepository.findByEmployeeIdAndTimestampBetweenOrderByTimestampDesc(any(), any(), any()))
                .thenReturn(List.of(attendanceLog));
        when(attendanceLogMapper.toResponse(attendanceLog)).thenReturn(attendanceLogResponse);

        List<AttendanceLogResponse> result = attendanceLogService.getMyLogs(
                "nv@company.com", 1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get my logs throws exception when employee not found")
    void getMyLogs_throwsWhenEmployeeNotFound() {
        when(employeeRepository.findByEmailAndCompanyId("unknown@company.com", 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceLogService.getMyLogs(
                "unknown@company.com", 1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Test
    @DisplayName("Register nfc swipe key check branches")
    void registerNfcSwipe_keyCheck_branches() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("C001");
        request.setCardUid("CARD123");
        
        // Case 1: expectedIotApiKey is null
        ReflectionTestUtils.setField(attendanceLogService, "expectedIotApiKey", null);
        
        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "wrong"))
                .isInstanceOf(AppException.class);

        // Case 2: expectedIotApiKey is blank
        ReflectionTestUtils.setField(attendanceLogService, "expectedIotApiKey", "   ");
        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "wrong"))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("Register nfc swipe handles null request fields")
    void registerNfcSwipe_nullRequestFields() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode(null);
        request.setEmployeeCode(null);
        request.setCardUid(null);

        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.EMPLOYEE_NOT_FOUND.getDefaultMessage());
    }

    @Test
    @DisplayName("Register nfc swipe filters employees in stream")
    void registerNfcSwipe_employeeFilter_findAllByNfcCardUidIgnoreCase() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("C001");
        request.setCardUid("CARD123");

        Company foundCompany = Company.builder().build();
        ReflectionTestUtils.setField(foundCompany, "id", 1L);
        when(companyRepository.findByCompanyCode("C001")).thenReturn(Optional.of(foundCompany));

        // Create inactive/invalid employees to trigger stream filter branches
        Employee empDiffCompany = new Employee();
        ReflectionTestUtils.setField(empDiffCompany, "id", 2L);
        Company otherComp = Company.builder().build();
        ReflectionTestUtils.setField(otherComp, "id", 99L);
        empDiffCompany.setCompany(otherComp);
        empDiffCompany.setStatus(EmployeeStatus.ACTIVE);

        Employee empDeleted = new Employee();
        ReflectionTestUtils.setField(empDeleted, "id", 3L);
        empDeleted.setCompany(foundCompany);
        empDeleted.setDeletedAt(Instant.now());

        Employee empResigned = new Employee();
        ReflectionTestUtils.setField(empResigned, "id", 4L);
        empResigned.setCompany(foundCompany);
        empResigned.setStatus(EmployeeStatus.RESIGNED);

        Employee empTerminated = new Employee();
        ReflectionTestUtils.setField(empTerminated, "id", 5L);
        empTerminated.setCompany(foundCompany);
        empTerminated.setStatus(EmployeeStatus.TERMINATED);

        // First call inside company block returns list with invalid company employee.
        // Second call (outer fallback check) returns empty list.
        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD123"))
                .thenReturn(List.of(empDiffCompany, empDeleted, empResigned, empTerminated))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(ErrorCode.EMPLOYEE_NOT_FOUND.getDefaultMessage());
    }

    @Test
    @DisplayName("Register nfc swipe falls back to employee code inside company block")
    void registerNfcSwipe_findByEmployeeCodeAndCompanyId_fallback() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("C001");
        request.setCardUid("CARD123");
        request.setEmployeeCode("EMP123");

        Company foundCompany = Company.builder().build();
        ReflectionTestUtils.setField(foundCompany, "id", 1L);
        when(companyRepository.findByCompanyCode("C001")).thenReturn(Optional.of(foundCompany));

        // findAllByNfcCardUidIgnoreCase returns empty (or null/filtered employee)
        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD123")).thenReturn(Collections.emptyList());

        // findByEmployeeCodeAndCompanyId returns active employee
        Employee activeEmp = new Employee();
        ReflectionTestUtils.setField(activeEmp, "id", 10L);
        activeEmp.setCompany(foundCompany);
        activeEmp.setStatus(EmployeeStatus.ACTIVE);
        activeEmp.setNfcCardUid(null);

        when(employeeRepository.findByEmployeeCodeAndCompanyId("EMP123", 1L)).thenReturn(Optional.of(activeEmp));
        when(employeeRepository.save(any(Employee.class))).thenReturn(activeEmp);

        Attendance attendance = Attendance.builder()
                .employee(activeEmp).company(foundCompany)
                .workDate(LocalDate.now()).status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED).build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(10L), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(any())).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Register nfc swipe card in use check branches")
    void registerNfcSwipe_cardInUseCheck_branches() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCardUid("CARD123");

        employee.setNfcCardUid(null); // needs auto-binding

        // 1. Same employee ID (should be filtered out, cardInUseByOtherActiveEmp = false)
        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD123"))
                .thenReturn(List.of(employee));

        when(employeeRepository.save(any())).thenReturn(employee);

        Attendance attendance = Attendance.builder()
                .employee(employee).company(company)
                .workDate(LocalDate.now()).status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED).build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(anyLong(), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any())).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(any())).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Register nfc swipe falls back to employee code outside company block")
    void registerNfcSwipe_fallbackOutsideCompany() {
        NfcSwipeRequest request = new NfcSwipeRequest();
        request.setCompanyCode("");
        request.setCardUid("CARD123");
        request.setEmployeeCode("EMP123");

        // 1. first query (by cardUid) returns empty
        when(employeeRepository.findAllByNfcCardUidIgnoreCase("CARD123")).thenReturn(Collections.emptyList());

        // 2. second query (by employeeCode) returns active employee
        Employee activeEmp = new Employee();
        ReflectionTestUtils.setField(activeEmp, "id", 10L);
        activeEmp.setCompany(company);
        activeEmp.setStatus(EmployeeStatus.ACTIVE);
        activeEmp.setNfcCardUid("CARD123");

        when(employeeRepository.findAllByEmployeeCodeIgnoreCase("EMP123")).thenReturn(List.of(activeEmp));

        Attendance attendance = Attendance.builder()
                .employee(activeEmp).company(company)
                .workDate(LocalDate.now()).status(AttendanceStatus.ABSENT)
                .checkedStatus(CheckedStatus.NOT_CHECKED).build();

        when(attendanceRepository.findByEmployeeIdAndWorkDate(eq(10L), any())).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceLogRepository.save(any())).thenReturn(attendanceLog);
        when(attendanceLogMapper.toResponse(any())).thenReturn(attendanceLogResponse);

        AttendanceLogResponse result = attendanceLogService.registerNfcSwipe(request, "IoT-Default-Auth-Key-2026");
        assertThat(result).isNotNull();
    }
}