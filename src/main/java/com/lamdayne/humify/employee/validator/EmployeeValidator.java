package com.lamdayne.humify.employee.validator;

import com.lamdayne.humify.branch.service.BranchAccessService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.department.service.DepartmentAccessService;
import com.lamdayne.humify.employee.dto.request.CreateEmployeeRequest;
import com.lamdayne.humify.employee.dto.request.TransferEmployeeRequest;
import com.lamdayne.humify.employee.dto.request.UpdateEmployeePositionRequest;
import com.lamdayne.humify.position.service.PositionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.repository.EmployeeRepository;

@Component
@RequiredArgsConstructor
public class EmployeeValidator {

    private final BranchAccessService branchAccessService;
    private final PositionAccessService positionAccessService;
    private final DepartmentAccessService departmentAccessService;
    private final EmployeeRepository employeeRepository;

    public void validateRefs(Long companyId, CreateEmployeeRequest request) {
        if (!branchAccessService.existsByIdAndCompanyId(request.getBranchId(), companyId)) {
            throw new AppException(ErrorCode.BRANCH_NOT_FOUND);
        }

        if (!departmentAccessService.existsByIdAndBranchId(request.getDepartmentId(), request.getBranchId())) {
            throw new AppException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        if (!positionAccessService.existsByIdAndCompanyId(request.getPositionId(), companyId)) {
            throw new AppException(ErrorCode.POSITION_NOT_FOUND);
        }
    }

    public void validateNfcCardUid(String nfcCardUid, Long currentEmployeeId) {
        if (nfcCardUid == null || nfcCardUid.isBlank()) {
            return;
        }

        boolean inUseByActiveEmployee = employeeRepository.findAllByNfcCardUidIgnoreCase(nfcCardUid.trim()).stream()
                .anyMatch(e -> e.getDeletedAt() == null
                        && e.getStatus() != EmployeeStatus.RESIGNED
                        && e.getStatus() != EmployeeStatus.TERMINATED
                        && (currentEmployeeId == null || !e.getId().equals(currentEmployeeId)));

        if (inUseByActiveEmployee) {
            throw new AppException(ErrorCode.NFC_CARD_ALREADY_EXISTS);
        }
    }

    // transfer
    public void validateTransfer(Long companyId, TransferEmployeeRequest request) {
        if (!branchAccessService.existsByIdAndCompanyId(request.getBranchId(), companyId)) {
            throw new AppException(ErrorCode.BRANCH_NOT_FOUND);
        }

        if (!departmentAccessService.existsByIdAndBranchId(request.getDepartmentId(), request.getBranchId())) {
            throw new AppException(ErrorCode.DEPARTMENT_NOT_FOUND);
        }

        if (request.getPositionId() != null &&
                !positionAccessService.existsByIdAndCompanyId(request.getPositionId(), companyId)
        ) {
                throw new AppException(ErrorCode.POSITION_NOT_FOUND);
        }
    }

    // position
    public void validatePosition(Long companyId, UpdateEmployeePositionRequest request) {
        if (!positionAccessService.existsByIdAndCompanyId(request.getPositionId(), companyId)) {
            throw new AppException(ErrorCode.POSITION_NOT_FOUND);
        }
    }

}

