package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateEmployeeWorkExperienceRequest {
    @NotBlank(message = "COMPANY_NAME_REQUIRED")
    private String companyName;

    @NotBlank(message = "POSITION_REQUIRED")
    private String position;

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String reasonForLeaving;
}
