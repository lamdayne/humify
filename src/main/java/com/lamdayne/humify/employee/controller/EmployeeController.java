package com.lamdayne.humify.employee.controller;

import com.lamdayne.humify.attendance.dto.request.UpdateLeaveBalanceRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveBalanceResponse;
import com.lamdayne.humify.attendance.service.LeaveBalanceService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.employee.dto.request.*;
import com.lamdayne.humify.employee.dto.response.EmployeeCertificationResponse;
import com.lamdayne.humify.employee.dto.response.EmployeeEducationResponse;
import com.lamdayne.humify.employee.dto.response.EmployeeIdDocumentResponse;
import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import com.lamdayne.humify.employee.service.EmployeeCertificationService;
import com.lamdayne.humify.employee.service.EmployeeEducationService;
import com.lamdayne.humify.employee.service.EmployeeIdDocumentService;
import com.lamdayne.humify.employee.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeCertificationService employeeCertificationService;
    private final EmployeeIdDocumentService idDocumentService;
    private final EmployeeEducationService employeeEducationService;
    private final LeaveBalanceService leaveBalanceService;

    @GetMapping("/{employeeId}/leave-balances")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getLeaveBalances(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year
    ) {
        List<LeaveBalanceResponse> response = leaveBalanceService.getLeaveBalances(employeeId, year);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.LEAVE_BALANCE_READ_SUCCESS, response));
    }

    @PutMapping("/{employeeId}/leave-balances")
    public ResponseEntity<ApiResponse<LeaveBalanceResponse>> updateLeaveBalance(
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateLeaveBalanceRequest request
    ) {
        LeaveBalanceResponse response = leaveBalanceService.updateLeaveBalance(employeeId, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.LEAVE_BALANCE_UPDATE_SUCCESS, response));
    }

    @PostMapping("/{employeeId}/id-documents")
    public ResponseEntity<ApiResponse<EmployeeIdDocumentResponse>> createDocument(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeIdDocumentRequest request
    ) {
        EmployeeIdDocumentResponse response = idDocumentService.addDocument(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_ID_DOCUMENT_CREATE_SUCCESS, response));
    }

    @GetMapping("/{employeeId}/id-documents")
    public ResponseEntity<ApiResponse<List<EmployeeIdDocumentResponse>>> getDocuments(
            @PathVariable Long employeeId
    ) {
        List<EmployeeIdDocumentResponse> response = idDocumentService.getDocuments(employeeId);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_ID_DOCUMENT_READ_SUCCESS, response));
    }

    @GetMapping("/{employeeId}/id-documents/{id}")
    public ResponseEntity<ApiResponse<EmployeeIdDocumentResponse>> getDocumentDetail(
            @PathVariable Long employeeId,
            @PathVariable Long id
    ) {
        EmployeeIdDocumentResponse response = idDocumentService.getDocumentDetail(employeeId, id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_ID_DOCUMENT_READ_SUCCESS, response));
    }

    @PutMapping("/{employeeId}/id-documents/{id}")
    public ResponseEntity<ApiResponse<EmployeeIdDocumentResponse>> updateDocument(
            @PathVariable Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody EmployeeIdDocumentRequest request
    ) {
        EmployeeIdDocumentResponse response = idDocumentService.updateDocument(employeeId, id, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_ID_DOCUMENT_UPDATE_SUCCESS, response));
    }

    @DeleteMapping("/{employeeId}/id-documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long employeeId,
            @PathVariable Long id
    ) {
        idDocumentService.deleteDocument(employeeId, id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_ID_DOCUMENT_DELETE_SUCCESS, null));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_CREATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @RequestBody @Valid CreateEmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_CREATE_SUCCESS,
                        employeeService.createEmployee(request)
                ));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_READ', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_READ_SUCCESS,
                        employeeService.getAllEmployees(page, size, sorts)
                ));
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid UpdateEmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_UPDATE_SUCCESS,
                        "Update info employee successfully",
                        employeeService.updateEmployee(employeeId, request)
                ));
    }

    @PutMapping("/{employeeId}/transfer")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> transferEmployee(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid TransferEmployeeRequest request
    ) {
        employeeService.transferEmployee(employeeId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_TRANSFER_SUCCESS
                ));
    }

    @PutMapping("/{employeeId}/status")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> updateEmployeeStatus(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid UpdateEmployeeStatusRequest request
    ) {
        employeeService.updateEmployeeStatus(employeeId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_STATUS_UPDATE_SUCCESS
                ));
    }

    @PutMapping("/{employeeId}/position")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> updateEmployeePosition(
            @PathVariable("employeeId") Long employeeId,
            @RequestBody @Valid UpdateEmployeePositionRequest request
    ) {
        employeeService.updateEmployeePosition(employeeId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_POSITION_UPDATE_SUCCESS
                ));
    }

    @PostMapping("/{employeeId}/certifications")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_CREATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeCertificationResponse>> addCertification(
        @PathVariable(name = "employeeId") Long employeeId,
        @RequestBody @Valid CreateEmployeeCertificationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_CERTIFICATION_CREATE_SUCCESS,
                        employeeCertificationService.addEmployeeCertification(employeeId, request)
                ));
    }

    @GetMapping("/{employeeId}/certifications")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_READ', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeCertificationResponse>>> getCertification(
            @PathVariable(name = "employeeId") Long employeeId,
            @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
            @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
            @RequestParam(required = false) String... sorts
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_CERTIFICATION_READ_SUCCESS,
                        employeeCertificationService.getEmployeeCertifications(employeeId, page, size, sorts)
                ));
    }

    @GetMapping("/{employeeId}/certifications/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_READ', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeCertificationResponse>> getCertificationById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "employeeId") Long employeeId,
            @PathVariable(name = "id") Long id
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_CERTIFICATION_READ_SUCCESS,
                        employeeCertificationService.getEmployeeCertification(
                                userPrincipal, employeeId, id
                        )
                ));
    }

    @PutMapping("/{employeeId}/certifications/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeCertificationResponse>> updateCertification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable(name = "employeeId") Long employeeId,
            @PathVariable(name = "id") Long id,
            @RequestBody @Valid UpdateEmployeeCertificationRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_POSITION_UPDATE_SUCCESS,
                        employeeCertificationService.updateEmployeeCertification(
                                userPrincipal, employeeId, id, request
                        )
                ));
    }

    @DeleteMapping("/{employeeId}/certifications/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteCertification(
            @PathVariable(name = "employeeId") Long employeeId,
            @PathVariable(name = "id") Long id
    ) {
        employeeCertificationService.deleteEmployeeCertification(employeeId, id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_CERTIFICATION_DELETE_SUCCESS
                ));
    }

    // education của khiemlee
    @PostMapping("/{employeeId}/educations")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeEducationResponse>> createEducation(
            @PathVariable Long employeeId,
            @Valid @RequestBody CreateEmployeeEducationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_EDUCATION_CREATE_SUCCESS,
                        employeeEducationService.createEducation(employeeId, request)
                ));
    }

    @GetMapping("/{employeeId}/educations")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_READ', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<List<EmployeeEducationResponse>>> getEducations(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_EDUCATION_READ_SUCCESS,
                        employeeEducationService.getEducationsByEmployeeId(employeeId)
                ));
    }

    @GetMapping("/{employeeId}/educations/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_READ', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeEducationResponse>> getEducationDetail(
            @PathVariable Long employeeId,
            @PathVariable Long id) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_EDUCATION_READ_SUCCESS,
                        employeeEducationService.getEducationDetail(employeeId, id)
                ));
    }

    @PutMapping("/{employeeId}/educations/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<EmployeeEducationResponse>> updateEducation(
            @PathVariable Long employeeId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeEducationRequest request) {

        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.EMPLOYEE_EDUCATION_UPDATE_SUCCESS,
                        employeeEducationService.updateEducation(employeeId, id, request)
                ));
    }

    @DeleteMapping("/{employeeId}/educations/{id}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS', 'EMPLOYEE_UPDATE', 'EMPLOYEE_FULL')")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @PathVariable Long employeeId,
            @PathVariable Long id) {

        employeeEducationService.deleteEducation(employeeId, id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.EMPLOYEE_EDUCATION_DELETE_SUCCESS));
    }

}
