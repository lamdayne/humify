package com.lamdayne.humify.attendance.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateEmployeeShiftRequest {

    private Long workShiftId;
    private LocalDate startDate;
    private LocalDate endDate;
}
