package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeWorkExperienceRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeWorkExperienceResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeWorkExperience;
import com.lamdayne.humify.employee.mapper.EmployeeWorkExperienceMapper;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.repository.EmployeeWorkExperienceRepository;
import com.lamdayne.humify.employee.service.EmployeeWorkExperienceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeWorkExperienceServiceImpl Tests")
public class EmployeeWorkExperienceServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeWorkExperienceRepository employeeWorkExperienceRepository;
    @Mock private EmployeeWorkExperienceMapper employeeWorkExperienceMapper;

    @InjectMocks
    private EmployeeWorkExperienceServiceImpl employeeWorkExperienceService;

    private Employee employee;
    private EmployeeWorkExperience experience;
    private EmployeeWorkExperienceResponse response;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().build();
        employee.setId(10L);

        experience = EmployeeWorkExperience.builder()
                .employee(employee)
                .companyName("Prev Company")
                .position("Software Engineer")
                .build();
        experience.setId(200L);

        response = EmployeeWorkExperienceResponse.builder()
                .id(200L)
                .companyName("Prev Company")
                .position("Software Engineer")
                .build();
    }

    // ---- create ----

    @Test
    @DisplayName("Create work experience successfully")
    void create_success() {
        CreateEmployeeWorkExperienceRequest request = mock(CreateEmployeeWorkExperienceRequest.class);

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(employeeWorkExperienceMapper.toEntity(request)).thenReturn(experience);
        when(employeeWorkExperienceRepository.save(experience)).thenReturn(experience);
        when(employeeWorkExperienceMapper.toResponse(experience)).thenReturn(response);

        EmployeeWorkExperienceResponse result = employeeWorkExperienceService.create(10L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(200L);
        verify(employeeWorkExperienceRepository).save(experience);
    }

    @Test
    @DisplayName("Create work experience throws exception when employee not found")
    void create_employeeNotFound() {
        CreateEmployeeWorkExperienceRequest request = mock(CreateEmployeeWorkExperienceRequest.class);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeWorkExperienceService.create(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));

        verify(employeeWorkExperienceRepository, never()).save(any());
    }

    // ---- getAll ----

    @Test
    @DisplayName("Get all work experiences successfully")
    void getAll_success() {
        when(employeeWorkExperienceRepository.findByEmployeeId(10L)).thenReturn(List.of(experience));
        when(employeeWorkExperienceMapper.toResponse(experience)).thenReturn(response);

        List<EmployeeWorkExperienceResponse> result = employeeWorkExperienceService.getAll(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(200L);
    }

    // ---- getById ----

    @Test
    @DisplayName("Get work experience by ID successfully")
    void getById_success() {
        when(employeeWorkExperienceRepository.findByIdAndEmployeeId(200L, 10L)).thenReturn(Optional.of(experience));
        when(employeeWorkExperienceMapper.toResponse(experience)).thenReturn(response);

        EmployeeWorkExperienceResponse result = employeeWorkExperienceService.getById(10L, 200L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("Get work experience by ID throws exception when not found")
    void getById_notFound() {
        when(employeeWorkExperienceRepository.findByIdAndEmployeeId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeWorkExperienceService.getById(10L, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_WORK_EXPERIENCE_NOT_FOUND));
    }

    // ---- update ----

    @Test
    @DisplayName("Update work experience successfully")
    void update_success() {
        UpdateEmployeeWorkExperienceRequest request = mock(UpdateEmployeeWorkExperienceRequest.class);

        when(employeeWorkExperienceRepository.findByIdAndEmployeeId(200L, 10L)).thenReturn(Optional.of(experience));
        when(employeeWorkExperienceRepository.save(experience)).thenReturn(experience);
        when(employeeWorkExperienceMapper.toResponse(experience)).thenReturn(response);

        EmployeeWorkExperienceResponse result = employeeWorkExperienceService.update(10L, 200L, request);

        assertThat(result).isNotNull();
        verify(employeeWorkExperienceMapper).update(experience, request);
        verify(employeeWorkExperienceRepository).save(experience);
    }

    @Test
    @DisplayName("Update work experience throws exception when not found")
    void update_notFound() {
        UpdateEmployeeWorkExperienceRequest request = mock(UpdateEmployeeWorkExperienceRequest.class);
        when(employeeWorkExperienceRepository.findByIdAndEmployeeId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeWorkExperienceService.update(10L, 999L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_WORK_EXPERIENCE_NOT_FOUND));

        verify(employeeWorkExperienceRepository, never()).save(any());
    }

    // ---- delete ----

    @Test
    @DisplayName("Delete work experience successfully")
    void delete_success() {
        when(employeeWorkExperienceRepository.findByIdAndEmployeeId(200L, 10L)).thenReturn(Optional.of(experience));

        employeeWorkExperienceService.delete(10L, 200L);

        verify(employeeWorkExperienceRepository).delete(experience);
    }

    @Test
    @DisplayName("Delete work experience throws exception when not found")
    void delete_notFound() {
        when(employeeWorkExperienceRepository.findByIdAndEmployeeId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeWorkExperienceService.delete(10L, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_WORK_EXPERIENCE_NOT_FOUND));

        verify(employeeWorkExperienceRepository, never()).delete(any(EmployeeWorkExperience.class));
    }
}
