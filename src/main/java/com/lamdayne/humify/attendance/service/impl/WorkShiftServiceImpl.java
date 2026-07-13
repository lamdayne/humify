package com.lamdayne.humify.attendance.service.impl;

import com.lamdayne.humify.attendance.dto.request.CreateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.response.WorkShiftResponse;
import com.lamdayne.humify.attendance.entity.WorkShift;
import com.lamdayne.humify.attendance.mapper.WorkShiftMapper;
import com.lamdayne.humify.attendance.repository.WorkShiftRepository;
import com.lamdayne.humify.attendance.repository.WorkShiftSpecification;
import com.lamdayne.humify.attendance.service.WorkShiftService;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.search.SearchCriteriaParser;
import com.lamdayne.humify.common.search.SpecSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkShiftServiceImpl implements WorkShiftService {

    private final WorkShiftRepository workShiftRepository;
    private final WorkShiftSpecification workShiftSpecification;
    private final WorkShiftMapper workShiftMapper;

    @Override
    @Transactional
    public WorkShiftResponse createWorkShift(CreateWorkShiftRequest request) {
        if (workShiftRepository.existsByCompanyIdAndShiftCodeAndDeletedAtIsNull(request.getShiftCode())) {
            throw new AppException(ErrorCode.SHIFT_CODE_ALREADY_EXISTS);
        }

        validateShiftTimes(request.getStartTime(), request.getEndTime(), request.getBreakStartTime(), request.getBreakEndTime());

        WorkShift shift = workShiftMapper.toEntity(request);
        shift.setStatus(Boolean.TRUE);

        return workShiftMapper.toResponse(workShiftRepository.save(shift));
    }

    @Override
    public PageResponse<WorkShiftResponse> getWorkShifts(Pageable pageable, String[] searchParams) {
        List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(searchParams != null ? searchParams : new String[0]);
        Specification<WorkShift> spec = workShiftSpecification.build(criteriaList);

        Page<WorkShift> page = workShiftRepository.findAll(spec, pageable);
        List<WorkShiftResponse> responses = page.stream().map(workShiftMapper::toResponse).toList();

        return PageResponse.<WorkShiftResponse>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .items(responses)
                .build();
    }

    @Override
    public WorkShiftResponse getWorkShiftDetail(Long id) {
        WorkShift shift = workShiftRepository.findById(id)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
        return workShiftMapper.toResponse(shift);
    }

    @Override
    @Transactional
    public WorkShiftResponse updateWorkShift(Long id, UpdateWorkShiftRequest request) {
        WorkShift shift = workShiftRepository.findById(id)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        Instant finalStart = request.getStartTime() != null ? request.getStartTime() : shift.getStartTime();
        Instant finalEnd = request.getEndTime() != null ? request.getEndTime() : shift.getEndTime();
        Instant finalBreakStart = request.getBreakStartTime() != null ? request.getBreakStartTime() : shift.getBreakStartTime();
        Instant finalBreakEnd = request.getBreakEndTime() != null ? request.getBreakEndTime() : shift.getBreakEndTime();
        validateShiftTimes(finalStart, finalEnd, finalBreakStart, finalBreakEnd);

        workShiftMapper.updateEntity(shift, request);
        return workShiftMapper.toResponse(workShiftRepository.save(shift));
    }

    @Override
    @Transactional
    public void deactivateWorkShift(Long id) {
        WorkShift shift = workShiftRepository.findById(id)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        shift.setStatus(Boolean.FALSE);
        workShiftRepository.save(shift);
    }

    private void validateShiftTimes(Instant start, Instant end, Instant breakStart, Instant breakEnd) {
        if (start == null || end == null) {
            throw new AppException(ErrorCode.SHIFT_TIME_INVALID);
        }

        if (start.isAfter(end) || start.equals(end)) {
            throw new AppException(ErrorCode.SHIFT_TIME_INVALID);
        }

        if (breakStart != null && breakEnd != null) {
            if (breakStart.isBefore(start) || breakEnd.isAfter(end) || breakStart.isAfter(breakEnd) || breakStart.equals(breakEnd)) {
                throw new AppException(ErrorCode.SHIFT_TIME_INVALID);
            }
        }
    }
}