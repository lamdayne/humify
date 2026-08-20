package com.lamdayne.humify.attendance.dto.response;

import com.lamdayne.humify.employee.dto.response.EmployeeResponse;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class EmployeeShiftResponse {

    private Long id;
    private EmployeeResponse employee;
    private WorkShiftResponse workShift;
    private LocalDate startDate;
    private LocalDate endDate;
}
