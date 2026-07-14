package com.lamdayne.humify.employee.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeEducationRequest {

    private String degreeLevel;
    private String schoolName;
    private String major;
    private Integer startYear;
    private Integer endYear;
    private Double gpa;
    private String certificateFileUrl;
    private String note;
}
