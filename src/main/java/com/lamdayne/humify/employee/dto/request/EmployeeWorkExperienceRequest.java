package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkExperienceRequest {

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