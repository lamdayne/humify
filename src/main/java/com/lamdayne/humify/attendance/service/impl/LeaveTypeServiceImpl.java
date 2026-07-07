package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.CreateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateLeaveTypeRequest;
import com.lamdayne.humify.attendance.dto.response.LeaveTypeResponse;
import com.lamdayne.humify.attendance.entity.LeaveType;
import com.lamdayne.humify.attendance.mapper.LeaveTypeMapper;
//import com.lamdayne.humify.attendance.repository.LeaveRequestRepository;
import com.lamdayne.humify.attendance.repository.LeaveTypeRepository;
import com.lamdayne.humify.attendance.service.LeaveTypeService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final CompanyRepository companyRepository;
    private final LeaveTypeMapper leaveTypeMapper;
//    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getLeaveTypes(Long companyId) {
        return leaveTypeRepository.findByCompanyIdAndDeletedAtIsNull(companyId).stream()
                .map(leaveTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveTypeResponse createLeaveType(Long companyId, CreateLeaveTypeRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        if (leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(companyId, request.getCode())) {
            throw new AppException(ErrorCode.LEAVE_TYPE_CODE_EXISTED);
        }

        LeaveType leaveType = LeaveType.builder()
                .company(company)
                .name(request.getName())
                .code(request.getCode())
                .maxDays(request.getMaxDays())
                .paid(request.getIsPaid() != null ? request.getIsPaid() : true)
                .requiresAttachment(request.getRequiresAttachment() != null ? request.getRequiresAttachment() : false)
                .description(request.getDescription())
                .build();

        return leaveTypeMapper.toResponse(leaveTypeRepository.save(leaveType));
    }

    @Override
    @Transactional
    public LeaveTypeResponse updateLeaveType(Long id, Long companyId, UpdateLeaveTypeRequest request) {
        LeaveType leaveType = leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_TYPE_NOT_FOUND));

        // Nếu cập nhật code, phải check trùng lặp (ngoại trừ chính nó)
        if (request.getCode() != null && !request.getCode().equals(leaveType.getCode())) {
            if (leaveTypeRepository.existsByCompanyIdAndCodeAndDeletedAtIsNull(companyId, request.getCode())) {
                throw new AppException(ErrorCode.LEAVE_TYPE_CODE_EXISTED);
            }
            leaveType.setCode(request.getCode());
        }

        if (request.getName() != null) leaveType.setName(request.getName());
        if (request.getMaxDays() != null) leaveType.setMaxDays(request.getMaxDays());
        if (request.getIsPaid() != null) leaveType.setPaid(request.getIsPaid());
        if (request.getRequiresAttachment() != null) leaveType.setRequiresAttachment(request.getRequiresAttachment());
        if (request.getDescription() != null) leaveType.setDescription(request.getDescription());

        return leaveTypeMapper.toResponse(leaveTypeRepository.save(leaveType));
    }

    @Override
    @Transactional
    public void deleteLeaveType(Long id, Long companyId) {
        LeaveType leaveType = leaveTypeRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_TYPE_NOT_FOUND));

        // TODO: Mở comment đoạn này khi bạn đã có LeaveRequestRepository
        /*
        boolean isUsedInActiveRequests = leaveRequestRepository
                .existsByLeaveTypeIdAndStatusIn(id, List.of(LeaveRequestStatus.PENDING, LeaveRequestStatus.APPROVED));
        if (isUsedInActiveRequests) {
            throw new AppException(ErrorCode.LEAVE_TYPE_IN_USE);
        }
        */

        // Thực hiện Soft Delete
        leaveType.setDeletedAt(Instant.now());
        leaveTypeRepository.save(leaveType);
    }
}

