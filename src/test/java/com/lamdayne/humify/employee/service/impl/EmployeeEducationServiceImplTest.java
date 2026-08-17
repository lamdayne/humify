package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeEducationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeEducationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeEducationResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeEducation;
import com.lamdayne.humify.employee.mapper.EmployeeEducationMapper;
import com.lamdayne.humify.employee.repository.EmployeeEducationRepository;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
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
@DisplayName("EmployeeEducationServiceImpl Tests")
public class EmployeeEducationServiceImplTest {

    @Mock private EmployeeEducationRepository educationRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeEducationMapper educationMapper;

    @InjectMocks
    private EmployeeEducationServiceImpl educationService;

    private Employee employee;
    private EmployeeEducation education;
    private EmployeeEducationResponse educationResponse;

    @BeforeEach
    void setUp() {
        employee = Employee.builder().build();
        employee.setId(10L);

        education = EmployeeEducation.builder()
                .employee(employee)
                .degreeLevel("Bachelor")
                .schoolName("University")
                .major("Computer Science")
                .startYear(2020)
                .endYear(2024)
                .gpa(3.80)
                .build();
        education.setId(100L);

        educationResponse = EmployeeEducationResponse.builder()
                .id(100L)
                .degreeLevel("Bachelor")
                .schoolName("University")
                .major("Computer Science")
                .build();
    }

    // ---- createEducation ----

    @Test
    @DisplayName("Create education successfully")
    void createEducation_success() {
        CreateEmployeeEducationRequest request = mock(CreateEmployeeEducationRequest.class);
        when(request.getDegreeLevel()).thenReturn("Bachelor");
        when(request.getSchoolName()).thenReturn("University");
        when(request.getMajor()).thenReturn("Computer Science");
        when(request.getStartYear()).thenReturn(2020);
        when(request.getEndYear()).thenReturn(2024);
        when(request.getGpa()).thenReturn(3.80);
        when(request.getCertificateFileUrl()).thenReturn("http://cert.url");
        when(request.getNote()).thenReturn("Honors");

        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(educationRepository.save(any(EmployeeEducation.class))).thenReturn(education);
        when(educationMapper.toResponse(any(EmployeeEducation.class))).thenReturn(educationResponse);

        EmployeeEducationResponse result = educationService.createEducation(10L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        verify(educationRepository).save(any(EmployeeEducation.class));
    }

    @Test
    @DisplayName("Create education throws exception when employee not found")
    void createEducation_employeeNotFound() {
        CreateEmployeeEducationRequest request = mock(CreateEmployeeEducationRequest.class);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.createEducation(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));

        verify(educationRepository, never()).save(any());
    }

    // ---- getEducationsByEmployeeId ----

    @Test
    @DisplayName("Get educations by employee ID successfully")
    void getEducationsByEmployeeId_success() {
        when(employeeRepository.existsById(10L)).thenReturn(true);
        when(educationRepository.findByEmployeeIdOrderByStartYearDesc(10L)).thenReturn(List.of(education));
        when(educationMapper.toResponse(education)).thenReturn(educationResponse);

        List<EmployeeEducationResponse> result = educationService.getEducationsByEmployeeId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Get educations throws exception when employee not found")
    void getEducationsByEmployeeId_employeeNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> educationService.getEducationsByEmployeeId(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    // ---- getEducationDetail ----

    @Test
    @DisplayName("Get education detail successfully")
    void getEducationDetail_success() {
        when(educationRepository.findById(100L)).thenReturn(Optional.of(education));
        when(educationMapper.toResponse(education)).thenReturn(educationResponse);

        EmployeeEducationResponse result = educationService.getEducationDetail(10L, 100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Get education detail throws exception when education not found")
    void getEducationDetail_notFound() {
        when(educationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.getEducationDetail(10L, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Test
    @DisplayName("Get education detail throws exception when employee mismatch (access denied)")
    void getEducationDetail_accessDenied() {
        when(educationRepository.findById(100L)).thenReturn(Optional.of(education)); // employee ID is 10L

        assertThatThrownBy(() -> educationService.getEducationDetail(99L, 100L)) // requesting with 99L
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---- updateEducation ----

    @Test
    @DisplayName("Update education successfully updates non-null fields")
    void updateEducation_success() {
        UpdateEmployeeEducationRequest request = mock(UpdateEmployeeEducationRequest.class);
        when(request.getDegreeLevel()).thenReturn("Master");
        when(request.getSchoolName()).thenReturn("Ivy League");
        when(request.getMajor()).thenReturn("Data Science");
        when(request.getStartYear()).thenReturn(2024);
        when(request.getEndYear()).thenReturn(2026);
        when(request.getGpa()).thenReturn(3.90);
        when(request.getCertificateFileUrl()).thenReturn("http://cert2.url");
        when(request.getNote()).thenReturn("Summa Cum Laude");

        when(educationRepository.findById(100L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(educationMapper.toResponse(education)).thenReturn(educationResponse);

        educationService.updateEducation(10L, 100L, request);

        verify(educationRepository).save(education);
        assertThat(education.getDegreeLevel()).isEqualTo("Master");
        assertThat(education.getSchoolName()).isEqualTo("Ivy League");
        assertThat(education.getMajor()).isEqualTo("Data Science");
        assertThat(education.getStartYear()).isEqualTo(2024);
        assertThat(education.getEndYear()).isEqualTo(2026);
        assertThat(education.getGpa()).isEqualTo(3.90);
        assertThat(education.getCertificateFileUrl()).isEqualTo("http://cert2.url");
        assertThat(education.getNote()).isEqualTo("Summa Cum Laude");
    }

    @Test
    @DisplayName("Update education ignores null fields")
    void updateEducation_ignoresNullFields() {
        UpdateEmployeeEducationRequest request = mock(UpdateEmployeeEducationRequest.class);
        // all getters return null by default in mock

        when(educationRepository.findById(100L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(educationMapper.toResponse(education)).thenReturn(educationResponse);

        educationService.updateEducation(10L, 100L, request);

        verify(educationRepository).save(education);
        assertThat(education.getDegreeLevel()).isEqualTo("Bachelor"); // remains unchanged
    }

    @Test
    @DisplayName("Update education throws exception when access denied")
    void updateEducation_accessDenied() {
        UpdateEmployeeEducationRequest request = mock(UpdateEmployeeEducationRequest.class);
        when(educationRepository.findById(100L)).thenReturn(Optional.of(education));

        assertThatThrownBy(() -> educationService.updateEducation(99L, 100L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    // ---- deleteEducation ----

    @Test
    @DisplayName("Delete education successfully")
    void deleteEducation_success() {
        when(educationRepository.findById(100L)).thenReturn(Optional.of(education));

        educationService.deleteEducation(10L, 100L);

        verify(educationRepository).delete(education);
    }

    @Test
    @DisplayName("Delete education throws exception when access denied")
    void deleteEducation_accessDenied() {
        when(educationRepository.findById(100L)).thenReturn(Optional.of(education));

        assertThatThrownBy(() -> educationService.deleteEducation(99L, 100L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));

        verify(educationRepository, never()).delete(any());
    }
}
