package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.CreateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateWorkShiftRequest;
import com.lamdayne.humify.attendance.dto.response.WorkShiftResponse;
import com.lamdayne.humify.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface WorkShiftService {
    WorkShiftResponse createWorkShift(CreateWorkShiftRequest request);
    PageResponse<WorkShiftResponse> getWorkShifts(Pageable pageable, String[] searchParams);
    WorkShiftResponse getWorkShiftDetail(Long id);
    WorkShiftResponse updateWorkShift(Long id, UpdateWorkShiftRequest request);
    void deactivateWorkShift(Long id);
}