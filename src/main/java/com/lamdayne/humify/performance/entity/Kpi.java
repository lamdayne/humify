package com.lamdayne.humify.performance.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.performance.enums.KpiMetricType;
import com.lamdayne.humify.performance.enums.KpiStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kpis")
public class Kpi extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_review_id")
    private PerformanceReview performanceReview;

    @Column(nullable = false)
    private String title;

    private String description;
    @Builder.Default
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Enumerated(EnumType.STRING)
    @Column(
            name = "metric_type",
            nullable = false,
            columnDefinition = "kpi_metric_type"
    )
    private KpiMetricType metricType = KpiMetricType.MANUAL;
    @Column(nullable = false)
    private Double targetValue;

    @Builder.Default
    @Column(nullable = false)
    private Double currentValue = 0.0;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(nullable = false)
    private Double weight;
    @Builder.Default
    @Column(name = "score")
    private Double score = 0.0;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private KpiStatus status = KpiStatus.IN_PROGRESS;

}
