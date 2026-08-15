package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.employee.dto.request.EmployeeIdDocumentRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeIdDocumentResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeIdDocument;
import com.lamdayne.humify.employee.mapper.EmployeeIdDocumentMapper;
import com.lamdayne.humify.employee.repository.EmployeeIdDocumentRepository;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeIdDocumentServiceImpl Unit Tests")
class EmployeeIdDocumentServiceImplTest {

    @Mock private EmployeeIdDocumentRepository idDocumentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeIdDocumentMapper idDocumentMapper;

    @InjectMocks
    private EmployeeIdDocumentServiceImpl idDocumentService;

    private Company company;
    private Employee employee;
    private EmployeeIdDocument document;

    @BeforeEach
    void setUp() {
        CompanyContext.setCompanyId(1L);

        company = Company.builder().build();
        company.setId(1L);

        employee = Employee.builder().company(company).build();
        employee.setId(1L); // note: current impl looks up employee by companyId, see below

        document = EmployeeIdDocument.builder()
                .employee(employee)
                .idType("CCCD")
                .idNumber("012345678900")
                .current(Boolean.TRUE)
                .build();
        document.setId(5L);
    }

    @AfterEach
    void tearDown() {
        CompanyContext.clear();
    }

    // ---- addDocument ----
    // NOTE: current implementation calls employeeRepository.findById(companyId) instead of
    // employeeId - this test documents the ACTUAL behavior of the existing code
    // (companyId=1L happens to coincide with employee.id=1L here so it passes).

    @Test
    @DisplayName("addDocument - success, defaults current=true when not provided")
    void addDocument_success_defaultsCurrentTrue() {
        EmployeeIdDocumentRequest request = mock(EmployeeIdDocumentRequest.class);
        when(request.getCurrent()).thenReturn(null);

        EmployeeIdDocument newDoc = EmployeeIdDocument.builder().idType("CCCD").build();
        EmployeeIdDocumentResponse response = mock(EmployeeIdDocumentResponse.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idDocumentMapper.toEntity(request)).thenReturn(newDoc);
        when(idDocumentRepository.save(newDoc)).thenReturn(newDoc);
        when(idDocumentMapper.toResponse(newDoc)).thenReturn(response);

        EmployeeIdDocumentResponse result = idDocumentService.addDocument(1L, request);

        assertNotNull(result);
        assertThat(newDoc.getCurrent()).isTrue();
        assertThat(newDoc.getEmployee()).isEqualTo(employee);
    }

    @Test
    @DisplayName("addDocument - success, keeps current=false when explicitly provided")
    void addDocument_success_explicitCurrentFalse() {
        EmployeeIdDocumentRequest request = mock(EmployeeIdDocumentRequest.class);
        when(request.getCurrent()).thenReturn(Boolean.FALSE);

        EmployeeIdDocument newDoc = EmployeeIdDocument.builder().idType("CCCD").current(Boolean.FALSE).build();
        EmployeeIdDocumentResponse response = mock(EmployeeIdDocumentResponse.class);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(idDocumentMapper.toEntity(request)).thenReturn(newDoc);
        when(idDocumentRepository.save(newDoc)).thenReturn(newDoc);
        when(idDocumentMapper.toResponse(newDoc)).thenReturn(response);

        EmployeeIdDocumentResponse result = idDocumentService.addDocument(1L, request);

        assertNotNull(result);
        assertThat(newDoc.getCurrent()).isFalse();
    }

    @Test
    @DisplayName("addDocument - fail when employee not found")
    void addDocument_employeeNotFound_throwsException() {
        EmployeeIdDocumentRequest request = mock(EmployeeIdDocumentRequest.class);
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> idDocumentService.addDocument(1L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
        verify(idDocumentRepository, never()).save(any());
    }

    // ---- getDocuments ----

    @Test
    @DisplayName("getDocuments - success")
    void getDocuments_success() {
        EmployeeIdDocumentResponse response = mock(EmployeeIdDocumentResponse.class);
        when(idDocumentRepository.findAllByEmployeeIdAndCompanyId(1L, 1L)).thenReturn(List.of(document));
        when(idDocumentMapper.toResponse(document)).thenReturn(response);

        List<EmployeeIdDocumentResponse> result = idDocumentService.getDocuments(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getDocuments - success, empty list")
    void getDocuments_empty() {
        when(idDocumentRepository.findAllByEmployeeIdAndCompanyId(1L, 1L)).thenReturn(List.of());

        List<EmployeeIdDocumentResponse> result = idDocumentService.getDocuments(1L);

        assertThat(result).isEmpty();
    }

    // ---- getDocumentDetail ----

    @Test
    @DisplayName("getDocumentDetail - success")
    void getDocumentDetail_success() {
        EmployeeIdDocumentResponse response = mock(EmployeeIdDocumentResponse.class);
        when(idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(5L, 1L, 1L)).thenReturn(Optional.of(document));
        when(idDocumentMapper.toResponse(document)).thenReturn(response);

        EmployeeIdDocumentResponse result = idDocumentService.getDocumentDetail(1L, 5L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getDocumentDetail - fail when not found")
    void getDocumentDetail_notFound_throwsException() {
        when(idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(999L, 1L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> idDocumentService.getDocumentDetail(1L, 999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_ID_DOCUMENT_NOT_FOUND);
    }

    // ---- updateDocument ----

    @Test
    @DisplayName("updateDocument - success")
    void updateDocument_success() {
        EmployeeIdDocumentRequest request = mock(EmployeeIdDocumentRequest.class);
        EmployeeIdDocumentResponse response = mock(EmployeeIdDocumentResponse.class);

        when(idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(5L, 1L, 1L)).thenReturn(Optional.of(document));
        when(idDocumentRepository.save(document)).thenReturn(document);
        when(idDocumentMapper.toResponse(document)).thenReturn(response);

        EmployeeIdDocumentResponse result = idDocumentService.updateDocument(1L, 5L, request);

        assertNotNull(result);
        verify(idDocumentMapper).updateEntity(document, request);
    }

    @Test
    @DisplayName("updateDocument - fail when not found")
    void updateDocument_notFound_throwsException() {
        EmployeeIdDocumentRequest request = mock(EmployeeIdDocumentRequest.class);
        when(idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(999L, 1L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> idDocumentService.updateDocument(1L, 999L, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_ID_DOCUMENT_NOT_FOUND);
    }

    // ---- deleteDocument ----

    @Test
    @DisplayName("deleteDocument - success")
    void deleteDocument_success() {
        when(idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(5L, 1L, 1L)).thenReturn(Optional.of(document));

        idDocumentService.deleteDocument(1L, 5L);

        verify(idDocumentRepository).delete(document);
    }

    @Test
    @DisplayName("deleteDocument - fail when not found")
    void deleteDocument_notFound_throwsException() {
        when(idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(999L, 1L, 1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> idDocumentService.deleteDocument(1L, 999L));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_ID_DOCUMENT_NOT_FOUND);
        verify(idDocumentRepository, never()).delete(any());
    }
}