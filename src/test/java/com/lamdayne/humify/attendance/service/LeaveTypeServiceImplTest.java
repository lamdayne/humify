package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.CreateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveTypeResponse;
import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.enums.LeaveRequestStatus;
import com.lamdayne.humify.attendance.mapper.LeaveTypeMapper;
import com.lamdayne.humify.attendance.repository.LeaveRequestRepository;
import com.lamdayne.humify.attendance.repository.LeaveTypeRepository;
import com.lamdayne.humify.attendance.service.impl.LeaveTypeServiceImpl;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveTypeServiceImpl Tests")
class LeaveTypeServiceImplTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private LeaveTypeRepository leaveTypeRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private LeaveTypeMapper leaveTypeMapper;

    @InjectMocks
    private LeaveTypeServiceImpl leaveTypeService;

    private Company company;
    private LeaveType leaveType;
    private LeaveTypeResponse leaveTypeResponse;

    @BeforeEach
    void setUp() {
        company = Company.builder().build();

        leaveType = LeaveType.builder()
                .company(company)
                .name("Nghi phep nam")
                .code("ANNUAL_LEAVE")
                .maxDays(new BigDecimal("12"))
                .paid(true)
                .requiresAttachment(false)
                .description("Nghi phep nam tieu chuan")
                .build();

        leaveTypeResponse = LeaveTypeResponse.builder()
                .name("Nghi phep nam")
                .code("ANNUAL_LEAVE")
                .isPaid(true)
                .build();
    }

    // ---- getLeaveTypes ----

    @Test
    @DisplayName("Get leave types returns valid list")
    void getLeaveTypes_returnsList() {
        when(leaveTypeRepository.findByCompanyIdAndDeletedAtIsNull(1L)).thenReturn(List.of(leaveType));
        when(leaveTypeMapper.toResponse(leaveType)).thenReturn(leaveTypeResponse);

        List<LeaveTypeResponse> result = leaveTypeService.getLeaveTypes(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("ANNUAL_LEAVE");
    }

    @Test
    @DisplayName("Get leave types returns empty list")
    void getLeaveTypes_returnsEmpty() {
        when(leaveTypeRepository.findByCompanyIdAndDeletedAtIsNull(1L)).thenReturn(List.of());

        List<LeaveTypeResponse> result = leaveTypeService.getLeaveTypes(1L);

        assertThat(result).isEmpty();
    }

    // ---- createLeaveType ----

    @Test
    @DisplayName("Create leave type successfully with default booleans")
    void createLeaveType_success_defaultBooleans() {
        CreateLeaveTypeRequest request = new CreateLeaveTypeRequest();
        request.setName("Nghi om");
        request.setCode("SICK_LEAVE");
        request.setMaxDays(new BigDecimal("10"));
        request.setIsPaid(null);
        request.setRequiresAttachment(null);
        request.setDescription("Nghi om co luong");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(1L, "SICK_LEAVE")).thenReturn(false);
        when(leaveTypeRepository.save(any(LeaveType.class))).thenReturn(leaveType);
        when(leaveTypeMapper.toResponse(leaveType)).thenReturn(leaveTypeResponse);

        LeaveTypeResponse result = leaveTypeService.createLeaveType(1L, request);

        assertThat(result).isNotNull();
        verify(leaveTypeRepository).save(any(LeaveType.class));
    }

    @Test
    @DisplayName("Create leave type successfully with explicit booleans")
    void createLeaveType_success_withExplicitBooleans() {
        CreateLeaveTypeRequest request = new CreateLeaveTypeRequest();
        request.setName("Nghi khong luong");
        request.setCode("UNPAID_LEAVE");
        request.setIsPaid(false);
        request.setRequiresAttachment(true);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(1L, "UNPAID_LEAVE")).thenReturn(false);
        when(leaveTypeRepository.save(any(LeaveType.class))).thenReturn(leaveType);
        when(leaveTypeMapper.toResponse(leaveType)).thenReturn(leaveTypeResponse);

        LeaveTypeResponse result = leaveTypeService.createLeaveType(1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Create leave type throws exception when company not found")
    void createLeaveType_throwsWhenCompanyNotFound() {
        CreateLeaveTypeRequest request = new CreateLeaveTypeRequest();
        request.setName("X");
        request.setCode("X_CODE");

        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveTypeService.createLeaveType(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.COMPANY_NOT_FOUND));
    }

    @Test
    @DisplayName("Create leave type throws exception when code already exists")
    void createLeaveType_throwsWhenCodeExists() {
        CreateLeaveTypeRequest request = new CreateLeaveTypeRequest();
        request.setName("Nghi phep");
        request.setCode("ANNUAL_LEAVE");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(1L, "ANNUAL_LEAVE")).thenReturn(true);

        assertThatThrownBy(() -> leaveTypeService.createLeaveType(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.LEAVE_TYPE_CODE_EXISTED));

        verify(leaveTypeRepository, never()).save(any());
    }

    // ---- updateLeaveType ----

    @Test
    @DisplayName("Update leave type successfully with all fields")
    void updateLeaveType_success_allFields() {
        UpdateLeaveTypeRequest request = new UpdateLeaveTypeRequest();
        request.setName("Nghi phep moi");
        request.setCode("NEW_CODE");
        request.setMaxDays(new BigDecimal("15"));
        request.setIsPaid(false);
        request.setRequiresAttachment(true);
        request.setDescription("Mo ta moi");

        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(1L, "NEW_CODE")).thenReturn(false);
        when(leaveTypeRepository.save(any(LeaveType.class))).thenReturn(leaveType);
        when(leaveTypeMapper.toResponse(leaveType)).thenReturn(leaveTypeResponse);

        LeaveTypeResponse result = leaveTypeService.updateLeaveType(1L, 1L, request);

        assertThat(result).isNotNull();
        verify(leaveTypeRepository).save(leaveType);
    }

    @Test
    @DisplayName("Update leave type successfully when keeping same code")
    void updateLeaveType_success_sameCode() {
        UpdateLeaveTypeRequest request = new UpdateLeaveTypeRequest();
        request.setCode("ANNUAL_LEAVE"); // trung voi code hien tai
        request.setName("Updated Name");

        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.save(any())).thenReturn(leaveType);
        when(leaveTypeMapper.toResponse(any())).thenReturn(leaveTypeResponse);

        LeaveTypeResponse result = leaveTypeService.updateLeaveType(1L, 1L, request);

        assertThat(result).isNotNull();
        // Khi code khong thay doi, khong kiem tra duplicate
        verify(leaveTypeRepository, never()).existsByCompanyIdAndCodeAndDeletedAtIsNull(anyLong(), anyString());
    }

    @Test
    @DisplayName("Update leave type throws exception when not found")
    void updateLeaveType_throwsWhenNotFound() {
        UpdateLeaveTypeRequest request = new UpdateLeaveTypeRequest();

        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveTypeService.updateLeaveType(99L, 1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.LEAVE_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("Update leave type throws exception when new code already exists")
    void updateLeaveType_throwsWhenNewCodeExists() {
        UpdateLeaveTypeRequest request = new UpdateLeaveTypeRequest();
        request.setCode("SICK_LEAVE"); // doi sang code khac

        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(1L, "SICK_LEAVE")).thenReturn(true);

        assertThatThrownBy(() -> leaveTypeService.updateLeaveType(1L, 1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.LEAVE_TYPE_CODE_EXISTED));
    }

    @Test
    @DisplayName("Update leave type successfully ignoring null fields")
    void updateLeaveType_success_nullFieldsIgnored() {
        UpdateLeaveTypeRequest request = new UpdateLeaveTypeRequest();
        // Tat ca null - khong cap nhat gi

        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(leaveType));
        when(leaveTypeRepository.save(any())).thenReturn(leaveType);
        when(leaveTypeMapper.toResponse(any())).thenReturn(leaveTypeResponse);

        LeaveTypeResponse result = leaveTypeService.updateLeaveType(1L, 1L, request);

        assertThat(result).isNotNull();
    }

    // ---- deleteLeaveType ----

    @Test
    @DisplayName("Delete leave type successfully with soft delete")
    void deleteLeaveType_success() {
        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(leaveType));
        when(leaveRequestRepository.existsByLeaveTypeIdAndStatusIn(eq(1L), any())).thenReturn(false);

        leaveTypeService.deleteLeaveType(1L, 1L);

        assertThat(leaveType.getDeletedAt()).isNotNull();
        verify(leaveTypeRepository).save(leaveType);
    }

    @Test
    @DisplayName("Delete leave type throws exception when not found")
    void deleteLeaveType_throwsWhenNotFound() {
        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveTypeService.deleteLeaveType(99L, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.LEAVE_TYPE_NOT_FOUND));
    }

    @Test
    @DisplayName("Delete leave type throws exception when leave type is in use")
    void deleteLeaveType_throwsWhenInUse() {
        when(leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(leaveType));
        when(leaveRequestRepository.existsByLeaveTypeIdAndStatusIn(eq(1L), any())).thenReturn(true);

        assertThatThrownBy(() -> leaveTypeService.deleteLeaveType(1L, 1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.LEAVE_TYPE_IN_USE));

        verify(leaveTypeRepository, never()).save(any());
    }
}