package com.lamdayne.humify.employee.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.employee.dto.request.CreateContractRequest;
import com.lamdayne.humify.employee.dto.request.UpdateContractRequest;
import com.lamdayne.humify.employee.dto.response.EmployeeContractResponse;
import com.lamdayne.humify.employee.enums.ContractStatus;

public interface EmployeeContractService {
    EmployeeContractResponse createContract(CreateContractRequest request);
    PageResponse<EmployeeContractResponse> getContracts(Long employeeId, ContractStatus status, int page, int size, String... sorts);
    EmployeeContractResponse updateContract(Long id, UpdateContractRequest request);
    void deleteContract(Long id);
}