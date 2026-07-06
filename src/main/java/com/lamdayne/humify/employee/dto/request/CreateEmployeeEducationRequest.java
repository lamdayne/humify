package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEmployeeEducationRequest {

    @NotBlank(message = "DEGREE_LEVEL_REQUIRED")
    private String degreeLevel;

    @NotBlank(message = "SCHOOL_NAME_REQUIRED")
    private String schoolName;

    private String major;

    @NotNull(message = "START_YEAR_REQUIRED")
    private Integer startYear;

    private Integer endYear;
    private Double gpa;
    private String certificateFileUrl;
    private String note;
}
