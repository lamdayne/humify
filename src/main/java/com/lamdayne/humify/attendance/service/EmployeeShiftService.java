package com.lamdayne.humify.attendance.service;

import com.lamdayne.humify.attendance.dto.request.CreateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.request.UpdateEmployeeShiftRequest;
import com.lamdayne.humify.attendance.dto.response.EmployeeShiftResponse;
import com.lamdayne.humify.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface EmployeeShiftService {

    EmployeeShiftResponse assignShift(CreateEmployeeShiftRequest request);

    PageResponse<EmployeeShiftResponse> getEmployeeShifts(Pageable pageable, String[] searchParams);

    EmployeeShiftResponse getEmployeeShiftDetail(Long id);

    EmployeeShiftResponse updateEmployeeShift(Long id, UpdateEmployeeShiftRequest request);

    void deleteEmployeeShift(Long id);
}
