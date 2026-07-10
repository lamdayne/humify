package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.ApproveLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.CreateLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.RejectLeaveRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveRequestResponse;
import com.lamdayne.humify.attendance.entity.LeaveRequest;
import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.enums.LeaveRequestStatus;
import com.lamdayne.humify.attendance.enums.LeaveSessionType;
import com.lamdayne.humify.attendance.mapper.LeaveRequestMapper;
import com.lamdayne.humify.attendance.repository.LeaveRequestRepository;
import com.lamdayne.humify.attendance.repository.LeaveRequestSpecification;
import com.lamdayne.humify.attendance.repository.LeaveTypeRepository;
import com.lamdayne.humify.attendance.service.LeaveBalanceService;
import com.lamdayne.humify.attendance.service.LeaveRequestService;
import com.lamdayne.humify.attendance.service.LeaveValidationService;
import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.search.SearchCriteriaParser;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private static final BigDecimal HALF_DAY = new BigDecimal("0.5");

    private final UserService userService;
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveValidationService leaveValidationService;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestSpecification leaveRequestSpecification;

    @Override
    public LeaveRequestResponse createLeaveRequest(UserPrincipal userPrincipal, CreateLeaveRequest request) {
        Employee employee = userService.getUserById(userPrincipal.getId()).getEmployee();

        LeaveSessionType sessionType = resolveSessionType(request.getSessionType());
        leaveValidationService.validateDate(request.getStartDate(), request.getEndDate(), sessionType);

        LeaveType leaveType = getLeaveType(request.getLeaveTypeId());

        leaveValidationService.validateAttachment(leaveType, request.getAttachmentUrl());
        leaveValidationService.validateOverlap(employee.getId(), request.getStartDate(), request.getEndDate(), null);

        BigDecimal durationDays = calculateDurationDays(request.getStartDate(), request.getEndDate(), sessionType);

        leaveBalanceService.verifyAndReverseBalance(
                employee.getId(), leaveType, request.getStartDate(), request.getEndDate(), sessionType, durationDays
        );

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .company(employee.getCompany())
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .durationDays(durationDays)
                .sessionType(sessionType)
                .reason(request.getReason())
                .attachmentUrl(request.getAttachmentUrl())
                .status(LeaveRequestStatus.PENDING)
                .build();

        return leaveRequestMapper.toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public LeaveRequestResponse getLeaveRequestById(UserPrincipal userPrincipal, Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));

        User user = userService.getUserById(userPrincipal.getId());
        Long employeeId = user.getEmployee().getId();

        boolean isOwner = leaveRequest.getEmployee().getId().equals(employeeId);
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("EMPLOYEE_FULL"));
        boolean sameCompany = leaveRequest.getCompany().getId().equals(userPrincipal.getCompanyId());

        if (!(sameCompany && (isAdmin || isOwner))) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return leaveRequestMapper.toLeaveRequestResponse(leaveRequest);
    }

    @Override
    public LeaveRequestResponse updateLeaveRequest(UserPrincipal userPrincipal, Long id, UpdateLeaveRequest request) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));

        Employee employee = userService.getUserById(userPrincipal.getId()).getEmployee();

        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        if (!leaveRequest.getStatus().equals(LeaveRequestStatus.PENDING)) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_CANNOT_UPDATE);
        }

        LeaveSessionType sessionType = resolveSessionType(request.getSessionType());
        leaveValidationService.validateDate(request.getStartDate(), request.getEndDate(), sessionType);
        leaveValidationService.validateAttachment(leaveRequest.getLeaveType(), request.getAttachmentUrl());
        leaveValidationService.validateOverlap(
                employee.getId(), request.getStartDate(), request.getEndDate(), leaveRequest.getId()
        );

        BigDecimal newDuration = calculateDurationDays(request.getStartDate(), request.getEndDate(), sessionType);

        leaveBalanceService.updateLeaveBalanceForUpdate(
                employee.getId(),
                leaveRequest.getLeaveType(),
                leaveRequest,
                request,
                newDuration
        );

        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setDurationDays(newDuration);
        leaveRequest.setSessionType(sessionType);
        leaveRequest.setReason(request.getReason());
        leaveRequest.setAttachmentUrl(request.getAttachmentUrl());

        return leaveRequestMapper.toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public void cancelLeaveRequest(UserPrincipal userPrincipal, Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));

        Employee employee = userService.getUserById(userPrincipal.getId()).getEmployee();

        if (!leaveRequest.getEmployee().getId().equals(employee.getId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        LeaveRequestStatus currentStatus = leaveRequest.getStatus();
        if (currentStatus == LeaveRequestStatus.REJECTED || currentStatus == LeaveRequestStatus.CANCELLED) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_STATUS_INVALID);
        }

        if (currentStatus == LeaveRequestStatus.PENDING) {
            leaveBalanceService.releasePendingBalance(employee.getId(), leaveRequest.getLeaveType(), leaveRequest);
        } else if (currentStatus == LeaveRequestStatus.APPROVED) {
            if (!leaveRequest.getStartDate().isAfter(LocalDate.now())) {
                throw new AppException(ErrorCode.LEAVE_REQUEST_CANNOT_CANCELLED);
            }

            leaveBalanceService.releaseUsedBalance(employee.getId(), leaveRequest.getLeaveType(), leaveRequest);

            // attendance
        }

        leaveRequest.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);
    }

    @Override
    public LeaveRequestResponse approveLeaveRequest(UserPrincipal userPrincipal, Long id, ApproveLeaveRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(id);

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_STATUS_INVALID);
        }

        User approver = userService.getUserById(userPrincipal.getId());
        Employee employee = leaveRequest.getEmployee();
        LeaveType leaveType = leaveRequest.getLeaveType();

        leaveBalanceService.approveLeaveBalance(employee.getId(), leaveType, leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
        leaveRequest.setApprover(approver);
        leaveRequest.setApproverNote(request.getApproverNote());
        leaveRequest.setApprovedAt(Instant.now());

        // send email

        return leaveRequestMapper.toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public LeaveRequestResponse rejectLeaveRequest(UserPrincipal userPrincipal, Long id, RejectLeaveRequest request) {
        LeaveRequest leaveRequest = getLeaveRequest(id);

        if (leaveRequest.getStatus() != LeaveRequestStatus.PENDING) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_STATUS_INVALID);
        }

        User approver = userService.getUserById(userPrincipal.getId());
        Employee employee = leaveRequest.getEmployee();

        leaveBalanceService.rejectLeaveBalance(employee.getId(), leaveRequest.getLeaveType(), leaveRequest);

        leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
        leaveRequest.setApprover(approver);
        leaveRequest.setApproverNote(request.getApproverNote());
        leaveRequest.setApprovedAt(Instant.now());

        // send email

        return leaveRequestMapper.toLeaveRequestResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public PageResponse<LeaveRequestResponse> getLeaveRequests(Pageable pageable, String[] leaveRequest) {
        List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(leaveRequest);

        Specification<LeaveRequest> spec = leaveRequestSpecification.build(criteriaList);

        Page<LeaveRequest> page = leaveRequestRepository.findAll(spec, pageable);

        List<LeaveRequestResponse> responses = page.stream().map(leaveRequestMapper::toLeaveRequestResponse).toList();

        return PageResponse.<LeaveRequestResponse>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(responses)
                .build();
    }

    private LeaveRequest getLeaveRequest(long id) {
        return leaveRequestRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));
    }

    private LeaveSessionType resolveSessionType(String sessionType) {
        return sessionType != null ? LeaveSessionType.valueOf(sessionType) : LeaveSessionType.FULL_DAY;
    }

    private LeaveType getLeaveType(Long leaveTypeId) {
        return leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_TYPE_NOT_FOUND));
    }

    private BigDecimal calculateDurationDays(LocalDate startDate, LocalDate endDate, LeaveSessionType sessionType) {
        if (sessionType != LeaveSessionType.FULL_DAY) {
            return HALF_DAY;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return BigDecimal.valueOf(days);
    }

}
