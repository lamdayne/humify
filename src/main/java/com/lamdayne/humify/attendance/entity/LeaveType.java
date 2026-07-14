package com.lamdayne.humify.attendance.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "leave_types")
public class LeaveType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(name = "max_days", precision = 5, scale = 2)
    private BigDecimal maxDays;

    @Builder.Default
    @Column(nullable = false, name = "is_paid")
    private Boolean paid = Boolean.TRUE;

    @Builder.Default
    @Column(nullable = false)
    private Boolean requiresAttachment = Boolean.FALSE;

    private String description;

}
