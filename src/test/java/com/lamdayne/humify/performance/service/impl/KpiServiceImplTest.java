package com.lamdayne.humify.performance.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyAccessService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.performance.dto.request.CreateKpiRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiProgressRequest;
import com.lamdayne.humify.performance.dto.response.KpiResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.enums.KpiStatus;
import com.lamdayne.humify.performance.mapper.KpiMapper;
import com.lamdayne.humify.performance.repository.KpiRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiServiceImpl Unit Tests")
class KpiServiceImplTest {

    @Mock private KpiRepository kpiRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CompanyAccessService companyAccessService;
    @Mock private KpiMapper kpiMapper;

    @InjectMocks
    private KpiServiceImpl kpiService;

    private Company company;
    private Employee employee;
    private Kpi kpi;

    @BeforeEach
    void setUp() {
        CompanyContext.setCompanyId(1L);

        company = Company.builder().build();
        company.setId(1L);

        employee = Employee.builder().company(company).build();
        employee.setId(2L);

        kpi = Kpi.builder()
                .company(company)
                .employee(employee)
                .title("Sales target Q3")
                .targetValue(100.0)
                .currentValue(0.0)
                .weight(0.5)
                .status(KpiStatus.IN_PROGRESS)
                .build();
        kpi.setId(3L);
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    // ---- createKpi ----

    @Test
    @DisplayName("createKpi - success")
    void createKpi_success() {
        CreateKpiRequest request = mock(CreateKpiRequest.class);
        when(request.getWeight()).thenReturn(0.3);
        when(request.getTitle()).thenReturn("New KPI");
        when(request.getTargetValue()).thenReturn(50.0);
        when(request.getStartDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(request.getEndDate()).thenReturn(LocalDate.of(2026, 12, 31));

        KpiResponse response = mock(KpiResponse.class);

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(kpiRepository.sumWeightByEmployeeIdAndCompanyId(2L, 1L, null)).thenReturn(0.4);
        when(companyAccessService.getReferenceById(1L)).thenReturn(company);
        when(kpiRepository.save(any(Kpi.class))).thenAnswer(inv -> inv.getArgument(0));
        when(kpiMapper.toResponse(any(Kpi.class))).thenReturn(response);

        KpiResponse result = kpiService.createKpi(2L, request);

        assertNotNull(result);
        verify(kpiRepository).save(any(Kpi.class));
    }

    @Test
    @DisplayName("createKpi - fail when employee not found or not in company")
    void createKpi_employeeNotFound_throwsException() {
        CreateKpiRequest request = mock(CreateKpiRequest.class);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> kpiService.createKpi(999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("createKpi - fail when total weight exceeds 1.0")
    void createKpi_weightExceedsLimit_throwsException() {
        CreateKpiRequest request = mock(CreateKpiRequest.class);
        when(request.getWeight()).thenReturn(0.7);

        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(kpiRepository.sumWeightByEmployeeIdAndCompanyId(2L, 1L, null)).thenReturn(0.5);

        AppException exception = assertThrows(AppException.class,
                () -> kpiService.createKpi(2L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.KPI_WEIGHT_INVALID);
        verify(kpiRepository, never()).save(any());
    }

    // ---- getKpisByEmployee ----

    @Test
    @DisplayName("getKpisByEmployee - success")
    void getKpisByEmployee_success() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        KpiResponse response = mock(KpiResponse.class);

        when(kpiRepository.findAllByEmployeeIdAndDateRange(2L, 1L, start, end)).thenReturn(List.of(kpi));
        when(kpiMapper.toResponse(kpi)).thenReturn(response);

        List<KpiResponse> result = kpiService.getKpisByEmployee(2L, start, end);

        assertThat(result).hasSize(1);
    }

    // ---- updateKpi ----

    @Test
    @DisplayName("updateKpi - success")
    void updateKpi_success() {
        CreateKpiRequest request = mock(CreateKpiRequest.class);
        when(request.getWeight()).thenReturn(0.4);
        when(request.getTitle()).thenReturn("Updated KPI");
        when(request.getTargetValue()).thenReturn(80.0);

        KpiResponse response = mock(KpiResponse.class);

        when(kpiRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(kpi));
        when(kpiRepository.sumWeightByEmployeeIdAndCompanyId(2L, 1L, 3L)).thenReturn(0.2);
        when(kpiRepository.save(kpi)).thenReturn(kpi);
        when(kpiMapper.toResponse(kpi)).thenReturn(response);

        KpiResponse result = kpiService.updateKpi(3L, request);

        assertNotNull(result);
        assertThat(kpi.getTitle()).isEqualTo("Updated KPI");
        assertThat(kpi.getWeight()).isEqualTo(0.4);
    }

    @Test
    @DisplayName("updateKpi - fail when not found")
    void updateKpi_notFound_throwsException() {
        CreateKpiRequest request = mock(CreateKpiRequest.class);
        when(kpiRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> kpiService.updateKpi(999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.KPI_NOT_FOUND);
    }

    @Test
    @DisplayName("updateKpi - fail when new weight exceeds limit")
    void updateKpi_weightExceedsLimit_throwsException() {
        CreateKpiRequest request = mock(CreateKpiRequest.class);
        when(request.getWeight()).thenReturn(0.9);

        when(kpiRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(kpi));
        when(kpiRepository.sumWeightByEmployeeIdAndCompanyId(2L, 1L, 3L)).thenReturn(0.5);

        AppException exception = assertThrows(AppException.class,
                () -> kpiService.updateKpi(3L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.KPI_WEIGHT_INVALID);
        verify(kpiRepository, never()).save(any());
    }

    // ---- updateProgress ----

    @Test
    @DisplayName("updateProgress - success, marks ACHIEVED when currentValue >= targetValue")
    void updateProgress_achievesTarget() {
        UpdateKpiProgressRequest request = mock(UpdateKpiProgressRequest.class);
        when(request.getCurrentValue()).thenReturn(100.0);

        KpiResponse response = mock(KpiResponse.class);

        when(kpiRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(kpi));
        when(kpiRepository.save(kpi)).thenReturn(kpi);
        when(kpiMapper.toResponse(kpi)).thenReturn(response);

        KpiResponse result = kpiService.updateProgress(3L, request);

        assertNotNull(result);
        assertThat(kpi.getStatus()).isEqualTo(KpiStatus.ACHIEVED);
    }

    @Test
    @DisplayName("updateProgress - success, marks IN_PROGRESS when currentValue < targetValue")
    void updateProgress_stillInProgress() {
        UpdateKpiProgressRequest request = mock(UpdateKpiProgressRequest.class);
        when(request.getCurrentValue()).thenReturn(40.0);

        KpiResponse response = mock(KpiResponse.class);

        when(kpiRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(kpi));
        when(kpiRepository.save(kpi)).thenReturn(kpi);
        when(kpiMapper.toResponse(kpi)).thenReturn(response);

        KpiResponse result = kpiService.updateProgress(3L, request);

        assertNotNull(result);
        assertThat(kpi.getStatus()).isEqualTo(KpiStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateProgress - fail when not found")
    void updateProgress_notFound_throwsException() {
        UpdateKpiProgressRequest request = mock(UpdateKpiProgressRequest.class);
        when(kpiRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> kpiService.updateProgress(999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.KPI_NOT_FOUND);
    }

    // ---- deleteKpi ----

    @Test
    @DisplayName("deleteKpi - success")
    void deleteKpi_success() {
        when(kpiRepository.findByIdAndCompanyId(3L, 1L)).thenReturn(Optional.of(kpi));

        kpiService.deleteKpi(3L);

        verify(kpiRepository).delete(kpi);
    }

    @Test
    @DisplayName("deleteKpi - fail when not found")
    void deleteKpi_notFound_throwsException() {
        when(kpiRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> kpiService.deleteKpi(999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.KPI_NOT_FOUND);
        verify(kpiRepository, never()).delete(any());
    }
}