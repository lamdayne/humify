package com.lamdayne.humify.employee.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class EmployeeEducationResponse {

    private Long id;
    private Long employeeId;
    private String degreeLevel;
    private String schoolName;
    private String major;
    private Integer startYear;
    private Integer endYear;
    private Double gpa;
    private String certificateFileUrl;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}
