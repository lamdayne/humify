package com.lamdayne.humify.employee.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class UpdateEmployeeWorkExperienceRequest {
    private String companyName;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String reasonForLeaving;
}
