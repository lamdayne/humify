package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.CreateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.response.WorkShiftResponse;
import com.lamdayne.humify.attendance.entity.WorkShift;
import com.lamdayne.humify.attendance.mapper.WorkShiftMapper;
import com.lamdayne.humify.attendance.repository.WorkShiftRepository;
import com.lamdayne.humify.attendance.repository.WorkShiftSpecification;
import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("WorkShiftServiceImpl Unit Tests")
class WorkShiftServiceImplTest {

    @Mock private WorkShiftRepository workShiftRepository;
    @Mock private WorkShiftSpecification workShiftSpecification;
    @Mock private WorkShiftMapper workShiftMapper;
    @Mock private CompanyService companyService;

    @InjectMocks
    private WorkShiftServiceImpl workShiftService;

    private WorkShift workShift;
    private Company company;
    private Instant start;
    private Instant end;
    private Instant breakStart;
    private Instant breakEnd;

    @BeforeEach
    void setUp() {
        start = Instant.parse("2026-08-01T01:00:00Z");
        end = Instant.parse("2026-08-01T09:00:00Z");
        breakStart = Instant.parse("2026-08-01T05:00:00Z");
        breakEnd = Instant.parse("2026-08-01T06:00:00Z");

        workShift = WorkShift.builder()
                .shiftCode("SHIFT01")
                .name("Ca sang")
                .startTime(start)
                .endTime(end)
                .breakStartTime(breakStart)
                .breakEndTime(breakEnd)
                .gracePeriodMinutes(5)
                .status(Boolean.TRUE)
                .build();
        workShift.setId(1L);

        company = new Company();
        company.setId(1L);

        CompanyContext.setCompanyId(1L);
        when(companyService.getCompanyById(1L)).thenReturn(company);
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    // ---- createWorkShift ----

    @Test
    @DisplayName("createWorkShift - success")
    void createWorkShift_success() {
        CreateWorkShiftRequest request = mock(CreateWorkShiftRequest.class);
        when(request.getShiftCode()).thenReturn("SHIFT01");
        when(request.getStartTime()).thenReturn(start);
        when(request.getEndTime()).thenReturn(end);
        when(request.getBreakStartTime()).thenReturn(breakStart);
        when(request.getBreakEndTime()).thenReturn(breakEnd);

        WorkShiftResponse response = mock(WorkShiftResponse.class);

        when(workShiftRepository.existsByShiftCodeAndDeletedAtIsNull("SHIFT01")).thenReturn(false);
        when(workShiftMapper.toEntity(request)).thenReturn(workShift);
        when(workShiftRepository.save(workShift)).thenReturn(workShift);
        when(workShiftMapper.toResponse(workShift)).thenReturn(response);

        WorkShiftResponse result = workShiftService.createWorkShift(request);

        assertNotNull(result);
        assertThat(workShift.getStatus()).isTrue();
        assertThat(workShift.getCompany()).isEqualTo(company);
        verify(workShiftRepository).save(workShift);
    }

    @Test
    @DisplayName("createWorkShift - fail when shift code already exists")
    void createWorkShift_codeExists_throwsException() {
        CreateWorkShiftRequest request = mock(CreateWorkShiftRequest.class);
        when(request.getShiftCode()).thenReturn("SHIFT01");

        when(workShiftRepository.existsByShiftCodeAndDeletedAtIsNull("SHIFT01")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.createWorkShift(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_CODE_ALREADY_EXISTS);
        verify(workShiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("createWorkShift - fail when start/end time is null")
    void createWorkShift_nullStartTime_throwsException() {
        CreateWorkShiftRequest request = mock(CreateWorkShiftRequest.class);
        when(request.getShiftCode()).thenReturn("SHIFT02");
        when(request.getStartTime()).thenReturn(null);

        when(workShiftRepository.existsByShiftCodeAndDeletedAtIsNull("SHIFT02")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.createWorkShift(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_TIME_INVALID);
    }

    @Test
    @DisplayName("createWorkShift - fail when start time is after end time")
    void createWorkShift_startAfterEnd_throwsException() {
        CreateWorkShiftRequest request = mock(CreateWorkShiftRequest.class);
        when(request.getShiftCode()).thenReturn("SHIFT02");
        when(request.getStartTime()).thenReturn(end);
        when(request.getEndTime()).thenReturn(start);

        when(workShiftRepository.existsByShiftCodeAndDeletedAtIsNull("SHIFT02")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.createWorkShift(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_TIME_INVALID);
    }

    @Test
    @DisplayName("createWorkShift - fail when break time is outside shift time")
    void createWorkShift_breakTimeOutsideShift_throwsException() {
        CreateWorkShiftRequest request = mock(CreateWorkShiftRequest.class);
        when(request.getShiftCode()).thenReturn("SHIFT02");
        when(request.getStartTime()).thenReturn(start);
        when(request.getEndTime()).thenReturn(end);
        when(request.getBreakStartTime()).thenReturn(start.minus(1, ChronoUnit.HOURS));
        when(request.getBreakEndTime()).thenReturn(breakEnd);

        when(workShiftRepository.existsByShiftCodeAndDeletedAtIsNull("SHIFT02")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.createWorkShift(request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_TIME_INVALID);
    }

    // ---- getWorkShifts ----

    @Test
    @DisplayName("getWorkShifts - success")
    void getWorkShifts_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WorkShift> page = new PageImpl<>(List.of(workShift), pageable, 1);
        WorkShiftResponse response = mock(WorkShiftResponse.class);

        when(workShiftSpecification.build(anyList())).thenReturn(mock(Specification.class));
        when(workShiftRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(workShiftMapper.toResponse(workShift)).thenReturn(response);

        PageResponse<WorkShiftResponse> result = workShiftService.getWorkShifts(pageable, new String[]{});

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getWorkShifts - success, null searchParams, empty result")
    void getWorkShifts_nullSearchParams_emptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WorkShift> page = new PageImpl<>(List.of(), pageable, 0);

        when(workShiftSpecification.build(anyList())).thenReturn(mock(Specification.class));
        when(workShiftRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageResponse<WorkShiftResponse> result = workShiftService.getWorkShifts(pageable, null);

        assertThat(result.getItems()).isEmpty();
    }

    // ---- getWorkShiftDetail ----

    @Test
    @DisplayName("getWorkShiftDetail - success")
    void getWorkShiftDetail_success() {
        WorkShiftResponse response = mock(WorkShiftResponse.class);
        when(workShiftRepository.findById(1L)).thenReturn(Optional.of(workShift));
        when(workShiftMapper.toResponse(workShift)).thenReturn(response);

        WorkShiftResponse result = workShiftService.getWorkShiftDetail(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getWorkShiftDetail - fail when not found")
    void getWorkShiftDetail_notFound_throwsException() {
        when(workShiftRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.getWorkShiftDetail(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_NOT_FOUND);
    }

    @Test
    @DisplayName("getWorkShiftDetail - fail when soft deleted")
    void getWorkShiftDetail_softDeleted_throwsException() {
        workShift.setDeletedAt(Instant.now());
        when(workShiftRepository.findById(1L)).thenReturn(Optional.of(workShift));

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.getWorkShiftDetail(1L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_NOT_FOUND);
    }

    // ---- updateWorkShift ----

    @Test
    @DisplayName("updateWorkShift - success")
    void updateWorkShift_success() {
        UpdateWorkShiftRequest request = mock(UpdateWorkShiftRequest.class);
        when(request.getStartTime()).thenReturn(null);
        when(request.getEndTime()).thenReturn(null);
        when(request.getBreakStartTime()).thenReturn(null);
        when(request.getBreakEndTime()).thenReturn(null);

        WorkShiftResponse response = mock(WorkShiftResponse.class);

        when(workShiftRepository.findById(1L)).thenReturn(Optional.of(workShift));
        when(workShiftRepository.save(workShift)).thenReturn(workShift);
        when(workShiftMapper.toResponse(workShift)).thenReturn(response);

        WorkShiftResponse result = workShiftService.updateWorkShift(1L, request);

        assertNotNull(result);
        verify(workShiftMapper).updateEntity(workShift, request);
        verify(workShiftRepository).save(workShift);
    }

    @Test
    @DisplayName("updateWorkShift - fail when not found")
    void updateWorkShift_notFound_throwsException() {
        UpdateWorkShiftRequest request = mock(UpdateWorkShiftRequest.class);
        when(workShiftRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.updateWorkShift(999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_NOT_FOUND);
    }

    @Test
    @DisplayName("updateWorkShift - fail when resulting time invalid")
    void updateWorkShift_invalidTime_throwsException() {
        UpdateWorkShiftRequest request = mock(UpdateWorkShiftRequest.class);
        when(request.getStartTime()).thenReturn(end);
        when(request.getEndTime()).thenReturn(start);
        when(request.getBreakStartTime()).thenReturn(null);
        when(request.getBreakEndTime()).thenReturn(null);

        when(workShiftRepository.findById(1L)).thenReturn(Optional.of(workShift));

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.updateWorkShift(1L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_TIME_INVALID);
        verify(workShiftRepository, never()).save(any());
    }

    // ---- deactivateWorkShift ----

    @Test
    @DisplayName("deactivateWorkShift - success")
    void deactivateWorkShift_success() {
        when(workShiftRepository.findById(1L)).thenReturn(Optional.of(workShift));
        when(workShiftRepository.save(workShift)).thenReturn(workShift);

        workShiftService.deactivateWorkShift(1L);

        assertThat(workShift.getStatus()).isFalse();
        verify(workShiftRepository).save(workShift);
    }

    @Test
    @DisplayName("deactivateWorkShift - fail when not found")
    void deactivateWorkShift_notFound_throwsException() {
        when(workShiftRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> workShiftService.deactivateWorkShift(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SHIFT_NOT_FOUND);
        verify(workShiftRepository, never()).save(any());
    }
}