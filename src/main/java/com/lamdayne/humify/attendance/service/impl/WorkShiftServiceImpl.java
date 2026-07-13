    package com.lamdayne.humify.attendance.service.impl;

    import com.lamdayne.humify.attendance.dto.request.CreateWorkShiftRequest;
    import com.lamdayne.humify.attendance.dto.request.UpdateWorkShiftRequest;
    import com.lamdayne.humify.attendance.dto.response.WorkShiftResponse;
    import com.lamdayne.humify.attendance.entity.WorkShift;
    import com.lamdayne.humify.attendance.mapper.WorkShiftMapper;
    import com.lamdayne.humify.attendance.repository.WorkShiftRepository;
    import com.lamdayne.humify.attendance.repository.WorkShiftSpecification;
    import com.lamdayne.humify.attendance.service.WorkShiftService;
    import com.lamdayne.humify.auth.security.rls.CompanyContext;
    import com.lamdayne.humify.common.exception.AppException;
    import com.lamdayne.humify.common.exception.ErrorCode;
    import com.lamdayne.humify.common.response.PageResponse;
    import com.lamdayne.humify.common.search.SearchCriteriaParser;
    import com.lamdayne.humify.common.search.SpecSearchCriteria;
    import com.lamdayne.humify.company.entity.Company;
    import com.lamdayne.humify.company.service.CompanyAccessService;
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
        private final CompanyAccessService companyAccessService;
        private final WorkShiftMapper workShiftMapper;

        @Override
        @Transactional
        public WorkShiftResponse createWorkShift(CreateWorkShiftRequest request) {
            Long companyId = CompanyContext.getCompanyId();

            if (workShiftRepository.existsByCompanyIdAndShiftCodeAndDeletedAtIsNull(companyId, request.getShiftCode())) {
                throw new AppException(ErrorCode.SHIFT_CODE_ALREADY_EXISTS);
            }

            validateShiftTimes(request.getStartTime(), request.getEndTime(), request.getBreakStartTime(), request.getBreakEndTime());

            Company company = companyAccessService.getReferenceById(companyId);
            WorkShift shift = workShiftMapper.toEntity(request);
            shift.setCompany(company);
            shift.setStatus(Boolean.TRUE);

            return workShiftMapper.toResponse(workShiftRepository.save(shift));
        }

        @Override
        public PageResponse<WorkShiftResponse> getWorkShifts(Pageable pageable, String[] searchParams) {
            Long companyId = CompanyContext.getCompanyId();

            List<SpecSearchCriteria> criteriaList = SearchCriteriaParser.parse(searchParams != null ? searchParams : new String[0]);
            Specification<WorkShift> spec = workShiftSpecification.build(criteriaList);

            Specification<WorkShift> companySpec = (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
            Specification<WorkShift> finalSpec = companySpec.and(spec);

            Page<WorkShift> page = workShiftRepository.findAll(finalSpec, pageable);
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
            Long companyId = CompanyContext.getCompanyId();
            WorkShift shift = workShiftRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
                    .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));
            return workShiftMapper.toResponse(shift);
        }

        @Override
        @Transactional
        public WorkShiftResponse updateWorkShift(Long id, UpdateWorkShiftRequest request) {
            Long companyId = CompanyContext.getCompanyId();
            WorkShift shift = workShiftRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
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
            Long companyId = CompanyContext.getCompanyId();
            WorkShift shift = workShiftRepository.findByIdAndCompanyIdAndDeletedAtIsNull(id, companyId)
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