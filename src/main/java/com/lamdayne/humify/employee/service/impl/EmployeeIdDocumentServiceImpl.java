package com.lamdayne.humify.employee.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.dto.request.EmployeeIdDocumentRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeIdDocumentResponse;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.entity.EmployeeIdDocument;
import com.lamdayne.humify.employee.mapper.EmployeeIdDocumentMapper;
import com.lamdayne.humify.employee.repository.EmployeeIdDocumentRepository;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.employee.service.EmployeeIdDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeIdDocumentServiceImpl implements EmployeeIdDocumentService {

    private final EmployeeIdDocumentRepository idDocumentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeIdDocumentMapper idDocumentMapper;

    @Override
    @Transactional
    public EmployeeIdDocumentResponse addDocument(Long employeeId, EmployeeIdDocumentRequest request) {
        Long companyId = CompanyContext.getCompanyId();

        Employee employee = employeeRepository.findById(companyId)
                .filter(emp -> emp.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeIdDocument document = idDocumentMapper.toEntity(request);
        document.setEmployee(employee);

        if (request.getCurrent() == null) {
            document.setCurrent(Boolean.TRUE);
        }

        return idDocumentMapper.toResponse(idDocumentRepository.save(document));
    }

    @Override
    public List<EmployeeIdDocumentResponse> getDocuments(Long employeeId) {
        Long companyId = CompanyContext.getCompanyId();
        return idDocumentRepository.findAllByEmployeeIdAndCompanyId(employeeId, companyId)
                .stream()
                .map(idDocumentMapper::toResponse)
                .toList();
    }

    @Override
    public EmployeeIdDocumentResponse getDocumentDetail(Long employeeId, Long id) {
        Long companyId = CompanyContext.getCompanyId();
        EmployeeIdDocument document = idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(id, employeeId, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_DOCUMENT_NOT_FOUND));
        return idDocumentMapper.toResponse(document);
    }

    @Override
    @Transactional
    public EmployeeIdDocumentResponse updateDocument(Long employeeId, Long id, EmployeeIdDocumentRequest request) {
        Long companyId = CompanyContext.getCompanyId();
        EmployeeIdDocument document = idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(id, employeeId, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_DOCUMENT_NOT_FOUND));

        idDocumentMapper.updateEntity(document, request);
        return idDocumentMapper.toResponse(idDocumentRepository.save(document));
    }

    @Override
    @Transactional
    public void deleteDocument(Long employeeId, Long id) {
        Long companyId = CompanyContext.getCompanyId();
        EmployeeIdDocument document = idDocumentRepository.findByIdAndEmployeeIdAndCompanyId(id, employeeId, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_DOCUMENT_NOT_FOUND));
        idDocumentRepository.delete(document);
    }
}