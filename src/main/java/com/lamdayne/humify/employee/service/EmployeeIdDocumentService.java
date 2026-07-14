package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.employee.dto.request.EmployeeIdDocumentRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeIdDocumentResponse;

import java.util.List;

public interface EmployeeIdDocumentService {
    EmployeeIdDocumentResponse addDocument(Long employeeId, EmployeeIdDocumentRequest request);
    List<EmployeeIdDocumentResponse> getDocuments(Long employeeId);
    EmployeeIdDocumentResponse getDocumentDetail(Long employeeId, Long id);
    EmployeeIdDocumentResponse updateDocument(Long employeeId, Long id, EmployeeIdDocumentRequest request);
    void deleteDocument(Long employeeId, Long id);
}