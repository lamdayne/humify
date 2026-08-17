package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.UpdateAttendanceManualRequest;
import com.lamdayne.humify.attendance.dto.response.AttendanceDetailResponse;
import com.lamdayne.humify.attendance.dto.response.AttendanceSummaryReportResponse;
import com.lamdayne.humify.attendance.entity.Attendance;
import com.lamdayne.humify.attendance.entity.WorkShift;
import com.lamdayne.humify.attendance.enums.AttendanceStatus;
import com.lamdayne.humify.attendance.enums.CheckedStatus;
import com.lamdayne.humify.attendance.repository.AttendanceRepository;
import com.lamdayne.humify.attendance.repository.AttendanceSpecification;
import com.lamdayne.humify.attendance.repository.WorkShiftRepository;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceServiceImpl Unit Tests")
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private AttendanceSpecification attendanceSpecification;
    @Mock
    private WorkShiftRepository workShiftRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee employee;
    private User user;
    private Attendance attendance;
    private WorkShift workShift;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .fullName("Nguyen Van A")
                .employeeCode("EMP001")
                .build();
        employee.setId(1L);

        user = User.builder()
                .employee(employee)
                .build();
        user.setId(10L);

        workShift = WorkShift.builder()
                .shiftCode("SHIFT01")
                .build();
        workShift.setId(5L);

        attendance = Attendance.builder()
                .employee(employee)
                .workShift(workShift)
                .workDate(LocalDate.of(2026, 8, 1))
                .checkedStatus(CheckedStatus.NOT_CHECKED)
                .status(AttendanceStatus.PRESENT)
                .workedHours(BigDecimal.ZERO)
                .otHours(BigDecimal.ZERO)
                .workPoints(BigDecimal.ONE)
                .isModified(Boolean.FALSE)
                .build();
        attendance.setId(100L);
    }

    // ---- getHRView ----

    @Test
    @DisplayName("getHRView - success with data")
    void getHRView_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Attendance> page = new PageImpl<>(List.of(attendance), pageable, 1);

        when(attendanceSpecification.build(anyList())).thenReturn(mock(Specification.class));
        when(attendanceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<AttendanceDetailResponse> result = attendanceService.getHRView(pageable, new String[]{});

        assertNotNull(result);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getEmployeeId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getEmployeeName()).isEqualTo("Nguyen Van A");
        verify(attendanceRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("getHRView - success empty result, null searchParams")
    void getHRView_emptyResult_nullSearchParams() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Attendance> page = new PageImpl<>(List.of(), pageable, 0);

        when(attendanceSpecification.build(anyList())).thenReturn(mock(Specification.class));
        when(attendanceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<AttendanceDetailResponse> result = attendanceService.getHRView(pageable, null);

        assertThat(result.getItems()).isEmpty();
    }

    // ---- getPersonalView ----

    @Test
    @DisplayName("getPersonalView - success")
    void getPersonalView_success() {
        when(userService.getUserById(10L)).thenReturn(user);
        when(attendanceSpecification.build(anyList())).thenReturn(mock(Specification.class));
        when(attendanceRepository.findAll(any(Specification.class))).thenReturn(List.of(attendance));

        List<AttendanceDetailResponse> result = attendanceService.getPersonalView(10L, new String[]{});

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getPersonalView - fail when user has no employee")
    void getPersonalView_noEmployee_throwsException() {
        User userWithoutEmployee = User.builder().employee(null).build();
        userWithoutEmployee.setId(10L);
        when(userService.getUserById(20L)).thenReturn(userWithoutEmployee);

        AppException exception = assertThrows(AppException.class,
                () -> attendanceService.getPersonalView(20L, new String[]{}));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
        verify(attendanceRepository, never()).findAll(any(Specification.class));
    }

    // ---- getSummaryReport ----

    @Test
    @DisplayName("getSummaryReport - success")
    void getSummaryReport_success() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);
        List<AttendanceSummaryReportResponse> mockResult = List.of(mock(AttendanceSummaryReportResponse.class));

        when(attendanceRepository.getSummaryReport(start, end)).thenReturn(mockResult);

        List<AttendanceSummaryReportResponse> result = attendanceService.getSummaryReport(start, end);

        assertThat(result).hasSize(1);
        verify(attendanceRepository).getSummaryReport(start, end);
    }

    @Test
    @DisplayName("getSummaryReport - fail when start date is null")
    void getSummaryReport_nullStartDate_throwsException() {
        AppException exception = assertThrows(AppException.class,
                () -> attendanceService.getSummaryReport(null, LocalDate.now()));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ATTENDANCE_START_DATE_REQUIRED);
    }

    @Test
    @DisplayName("getSummaryReport - fail when end date is null")
    void getSummaryReport_nullEndDate_throwsException() {
        AppException exception = assertThrows(AppException.class,
                () -> attendanceService.getSummaryReport(LocalDate.now(), null));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ATTENDANCE_END_DATE_REQUIRED);
    }

    @Test
    @DisplayName("getSummaryReport - fail when start date is after end date")
    void getSummaryReport_startAfterEnd_throwsException() {
        LocalDate start = LocalDate.of(2026, 8, 31);
        LocalDate end = LocalDate.of(2026, 8, 1);

        AppException exception = assertThrows(AppException.class,
                () -> attendanceService.getSummaryReport(start, end));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_FILTER_VALUE);
    }

    // ---- updateManual ----

    @Test
    @DisplayName("updateManual - success, full checkin/checkout, updates workShift")
    void updateManual_success_fullCheckInOut() {
        UpdateAttendanceManualRequest request = new UpdateAttendanceManualRequest();
        request.setManualCheckIn(Instant.parse("2026-08-01T01:00:00Z"));
        request.setManualCheckOut(Instant.parse("2026-08-01T09:00:00Z"));
        request.setWorkPoints(BigDecimal.ONE);
        request.setStatus("PRESENT");
        request.setModificationReason("Quen check out");
        request.setWorkShiftId(5L);
        request.setOtHours(BigDecimal.valueOf(1.5));

        when(attendanceRepository.findById(100L)).thenReturn(Optional.of(attendance));
        when(userService.getUserById(10L)).thenReturn(user);
        when(workShiftRepository.findById(5L)).thenReturn(Optional.of(workShift));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceDetailResponse result = attendanceService.updateManual(100L, 10L, request);

        assertNotNull(result);
        assertThat(result.getWorkedHours()).isEqualByComparingTo("8.00");
        assertThat(result.getCheckedStatus()).isEqualTo(CheckedStatus.CHECKED_OUT.name());
        assertThat(result.getIsModified()).isTrue();
        verify(attendanceRepository).save(attendance);
    }

    @Test
    @DisplayName("updateManual - only check-in, no check-out")
    void updateManual_onlyCheckIn() {
        UpdateAttendanceManualRequest request = new UpdateAttendanceManualRequest();
        request.setManualCheckIn(Instant.parse("2026-08-01T01:00:00Z"));
        request.setManualCheckOut(null);
        request.setWorkPoints(BigDecimal.ONE);
        request.setStatus("PRESENT");
        request.setModificationReason("Quen check out");

        when(attendanceRepository.findById(100L)).thenReturn(Optional.of(attendance));
        when(userService.getUserById(10L)).thenReturn(user);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceDetailResponse result = attendanceService.updateManual(100L, 10L, request);

        assertThat(result.getCheckedStatus()).isEqualTo(CheckedStatus.CHECKED_IN.name());
        assertThat(result.getWorkedHours()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("updateManual - no check-in, no check-out")
    void updateManual_noCheckInOut() {
        UpdateAttendanceManualRequest request = new UpdateAttendanceManualRequest();
        request.setManualCheckIn(null);
        request.setManualCheckOut(null);
        request.setWorkPoints(BigDecimal.ZERO);
        request.setStatus("ABSENT");
        request.setModificationReason("Nghi khong phep");

        when(attendanceRepository.findById(100L)).thenReturn(Optional.of(attendance));
        when(userService.getUserById(10L)).thenReturn(user);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceDetailResponse result = attendanceService.updateManual(100L, 10L, request);

        assertThat(result.getCheckedStatus()).isEqualTo(CheckedStatus.NOT_CHECKED.name());
        assertThat(result.getStatus()).isEqualTo("ABSENT");
    }

    @Test
    @DisplayName("updateManual - fail when attendance record not found")
    void updateManual_attendanceNotFound_throwsException() {
        UpdateAttendanceManualRequest request = new UpdateAttendanceManualRequest();
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> attendanceService.updateManual(999L, 10L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND);
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateManual - fail when work shift not found")
    void updateManual_workShiftNotFound_throwsException() {
        UpdateAttendanceManualRequest request = new UpdateAttendanceManualRequest();
        request.setManualCheckIn(Instant.now());
        request.setManualCheckOut(Instant.now());
        request.setWorkPoints(BigDecimal.ONE);
        request.setStatus("PRESENT");
        request.setModificationReason("test");
        request.setWorkShiftId(999L);

        when(attendanceRepository.findById(100L)).thenReturn(Optional.of(attendance));
        when(userService.getUserById(10L)).thenReturn(user);
        when(workShiftRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> attendanceService.updateManual(100L, 10L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_NOT_FOUND);
        verify(attendanceRepository, never()).save(any());
    }
}