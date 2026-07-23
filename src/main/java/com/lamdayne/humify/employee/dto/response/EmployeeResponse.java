package com.lamdayne.humify.employee.dto.response;

import com.lamdayne.humify.employee.enums.EmployeeStatus;
import com.lamdayne.humify.employee.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse implements Serializable {
    private String branchName;
    private String departmentName;
    private String positionName;
    private String employeeCode;
    private String fullName;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String email;
    private String phone;
    private String address;
    private LocalDate startDate;
    private EmployeeStatus status;
}
