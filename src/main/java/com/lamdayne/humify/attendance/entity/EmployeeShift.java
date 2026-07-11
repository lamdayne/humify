package com.lamdayne.humify.attendance.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee_shifts")
public class EmployeeShift extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_shift_id", nullable = false)
    private WorkShift workShift;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

}
