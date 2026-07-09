package com.lamdayne.humify.payroll.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.payroll.enums.PayrollPeriodStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payroll_periods")
public class PayrollPeriod extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal standardWorkDays = new BigDecimal("22.00");

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "payroll_period_status", nullable = false)
    private PayrollPeriodStatus status = PayrollPeriodStatus.DRAFT;

}
