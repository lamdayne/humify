package com.lamdayne.humify.employee.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_educations")
public class EmployeeEducation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String degreeLevel;

    private String schoolName;

    private String major;

    private Integer startYear;

    private Integer endYear;

    private Double gpa;

    private String certificateFileUrl;

    private String note;

}
