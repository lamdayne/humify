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
@Table(name = "employee_id_documents")
public class EmployeeIdDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 50)
    private String idType;

    @Column(nullable = false, length = 50)
    private String idNumber;

    private LocalDate issuedDate;

    private String issuedPlace;

    private String expiredDate;

    private String frontImageUrl;

    private String backImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean current =  Boolean.TRUE;

}
