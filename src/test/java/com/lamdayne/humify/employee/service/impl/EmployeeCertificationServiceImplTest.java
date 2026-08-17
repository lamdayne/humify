package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeeCertificationRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeCertificationResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeCertification;
import com.lamdayne.humify.employee.mapper.EmployeeCertificationMapper;
import com.lamdayne.humify.employee.repository.EmployeeCertificationRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeCertificationServiceImpl Tests")
public class EmployeeCertificationServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeCertificationMapper employeeCertificationMapper;
    @Mock private EmployeeCertificationRepository employeeCertificationRepository;

    @InjectMocks
    private EmployeeCertificationServiceImpl employeeCertificationService;

    private UserPrincipal userPrincipal;
    private Company company;
    private Employee employee;
    private EmployeeCertification employeeCert;
    private EmployeeCertificationResponse certResponse;

    @BeforeEach
    void setUp() {
        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .companyId(10L)
                .email("user@company.com")
                .build();

        company = Company.builder().build();
        company.setId(10L);

        employee = Employee.builder()
                .company(company)
                .fullName("John Doe")
                .build();
        employee.setId(100L);

        employeeCert = EmployeeCertification.builder()
                .employee(employee)
                .name("AWS Architect")
                .build();
        employeeCert.setId(500L);

        certResponse = EmployeeCertificationResponse.builder()
                .id(500L)
                .name("AWS Architect")
                .build();
    }

    // ---- addEmployeeCertification ----

    @Test
    @DisplayName("Add certification successfully")
    void addEmployeeCertification_success() {
        CreateEmployeeCertificationRequest request = mock(CreateEmployeeCertificationRequest.class);

        when(employeeRepository.findById(100L)).thenReturn(Optional.of(employee));
        when(employeeCertificationMapper.toEmployeeCertification(request)).thenReturn(employeeCert);
        when(employeeCertificationRepository.save(employeeCert)).thenReturn(employeeCert);
        when(employeeCertificationMapper.toEmployeeCertificationResponse(employeeCert)).thenReturn(certResponse);

        EmployeeCertificationResponse result = employeeCertificationService.addEmployeeCertification(100L, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(500L);
        verify(employeeCertificationRepository).save(employeeCert);
    }

    @Test
    @DisplayName("Add certification throws exception when employee not found")
    void addEmployeeCertification_employeeNotFound() {
        CreateEmployeeCertificationRequest request = mock(CreateEmployeeCertificationRequest.class);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeCertificationService.addEmployeeCertification(999L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));

        verify(employeeCertificationRepository, never()).save(any());
    }

    // ---- updateEmployeeCertification ----

    @Test
    @DisplayName("Update certification successfully when user belongs to same company")
    void updateEmployeeCertification_success() {
        UpdateEmployeeCertificationRequest request = mock(UpdateEmployeeCertificationRequest.class);

        when(employeeCertificationRepository.findByIdAndEmployeeId(500L, 100L)).thenReturn(Optional.of(employeeCert));
        when(employeeCertificationRepository.save(employeeCert)).thenReturn(employeeCert);
        when(employeeCertificationMapper.toEmployeeCertificationResponse(employeeCert)).thenReturn(certResponse);

        EmployeeCertificationResponse result = employeeCertificationService.updateEmployeeCertification(
                userPrincipal, 100L, 500L, request
        );

        assertThat(result).isNotNull();
        verify(employeeCertificationMapper).updateEmployeeCertification(employeeCert, request);
        verify(employeeCertificationRepository).save(employeeCert);
    }

    @Test
    @DisplayName("Update certification throws exception when different company (forbidden)")
    void updateEmployeeCertification_forbidden() {
        UpdateEmployeeCertificationRequest request = mock(UpdateEmployeeCertificationRequest.class);

        Company otherCompany = Company.builder().build();
        otherCompany.setId(99L);
        Employee otherEmployee = Employee.builder().company(otherCompany).build();
        otherEmployee.setId(101L);

        EmployeeCertification otherCert = EmployeeCertification.builder().employee(otherEmployee).build();
        otherCert.setId(500L);

        when(employeeCertificationRepository.findByIdAndEmployeeId(500L, 101L)).thenReturn(Optional.of(otherCert));

        assertThatThrownBy(() -> employeeCertificationService.updateEmployeeCertification(
                userPrincipal, 101L, 500L, request
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));

        verify(employeeCertificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update certification throws exception when employee company is null (forbidden)")
    void updateEmployeeCertification_nullCompany() {
        UpdateEmployeeCertificationRequest request = mock(UpdateEmployeeCertificationRequest.class);

        Employee otherEmployee = Employee.builder().company(null).build();
        otherEmployee.setId(101L);

        EmployeeCertification otherCert = EmployeeCertification.builder().employee(otherEmployee).build();
        otherCert.setId(500L);

        when(employeeCertificationRepository.findByIdAndEmployeeId(500L, 101L)).thenReturn(Optional.of(otherCert));

        assertThatThrownBy(() -> employeeCertificationService.updateEmployeeCertification(
                userPrincipal, 101L, 500L, request
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));

        verify(employeeCertificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update certification throws exception when certification not found")
    void updateEmployeeCertification_notFound() {
        UpdateEmployeeCertificationRequest request = mock(UpdateEmployeeCertificationRequest.class);
        when(employeeCertificationRepository.findByIdAndEmployeeId(999L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeCertificationService.updateEmployeeCertification(
                userPrincipal, 100L, 999L, request
        ))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_CERTIFICATION_NOT_FOUND));
    }

    // ---- getEmployeeCertification ----

    @Test
    @DisplayName("Get certification detail successfully")
    void getEmployeeCertification_success() {
        when(employeeCertificationRepository.findByIdAndEmployeeId(500L, 100L)).thenReturn(Optional.of(employeeCert));
        when(employeeCertificationMapper.toEmployeeCertificationResponse(employeeCert)).thenReturn(certResponse);

        EmployeeCertificationResponse result = employeeCertificationService.getEmployeeCertification(userPrincipal, 100L, 500L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Get certification detail throws exception when forbidden")
    void getEmployeeCertification_forbidden() {
        Company otherCompany = Company.builder().build();
        otherCompany.setId(99L);
        Employee otherEmployee = Employee.builder().company(otherCompany).build();
        EmployeeCertification otherCert = EmployeeCertification.builder().employee(otherEmployee).build();

        when(employeeCertificationRepository.findByIdAndEmployeeId(500L, 101L)).thenReturn(Optional.of(otherCert));

        assertThatThrownBy(() -> employeeCertificationService.getEmployeeCertification(userPrincipal, 101L, 500L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ---- deleteEmployeeCertification ----

    @Test
    @DisplayName("Delete certification successfully")
    void deleteEmployeeCertification_success() {
        when(employeeCertificationRepository.findByIdAndEmployeeId(500L, 100L)).thenReturn(Optional.of(employeeCert));

        employeeCertificationService.deleteEmployeeCertification(100L, 500L);

        verify(employeeCertificationRepository).delete(employeeCert);
    }

    @Test
    @DisplayName("Delete certification throws exception when not found")
    void deleteEmployeeCertification_notFound() {
        when(employeeCertificationRepository.findByIdAndEmployeeId(999L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeCertificationService.deleteEmployeeCertification(100L, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_CERTIFICATION_NOT_FOUND));

        verify(employeeCertificationRepository, never()).delete(any(EmployeeCertification.class));
    }

    // ---- getEmployeeCertifications ----

    @Test
    @DisplayName("Get paginated employee certifications successfully")
    void getEmployeeCertifications_success() {
        Page<EmployeeCertification> page = new PageImpl<>(List.of(employeeCert));
        when(employeeCertificationRepository.findByEmployeeId(eq(100L), any(Pageable.class))).thenReturn(page);
        when(employeeCertificationMapper.toEmployeeCertificationResponse(employeeCert)).thenReturn(certResponse);

        PageResponse<EmployeeCertificationResponse> result = employeeCertificationService.getEmployeeCertifications(100L, 1, 10, "id,asc");

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getId()).isEqualTo(500L);
    }
}
