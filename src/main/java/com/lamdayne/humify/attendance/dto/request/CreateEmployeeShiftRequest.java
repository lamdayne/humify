package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CreateEmployeeShiftRequest {

    @NotNull(message = "EMPLOYEE_ID_REQUIRED")
    private Long employeeId;

    @NotNull(message = "SHIFT_ID_REQUIRED")
    private Long workShiftId;

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startDate;

    private LocalDate endDate;
}
