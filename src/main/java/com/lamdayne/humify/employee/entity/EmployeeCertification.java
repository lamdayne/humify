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
@Table(name = "employee_certifications")
public class EmployeeCertification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String name;

    private String certificationCode;

    private String issuedBy;

    private LocalDate issuedDate;

    private LocalDate expiredDate;

    private String scoreOrLevel;

    private String certificationFileUrl;

}
