package com.lamdayne.humify.employee.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_work_experiences")
public class EmployeeWorkExperience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String companyName;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String reasonForLeaving;

}
