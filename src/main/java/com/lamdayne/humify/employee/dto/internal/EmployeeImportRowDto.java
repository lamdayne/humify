package com.lamdayne.humify.employee.dto.internal;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeImportRowDto {
    private int rowIndex;
    private String branchName;
    private String departmentName;
    private String email;
    private String fullName;
    private String positionName;
    private LocalDate dateOfBirth;
    private String gender;
    private LocalDate startDate;
    private String status;
    private String degree;
    private String schoolName;
    private String major;
    private Integer startYear;
    private Integer endYear;
    private Double gpa;
}
