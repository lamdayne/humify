package com.lamdayne.humify.performance.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performance_reviews")
public class PerformanceReview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private KpiTemplate template;

    @Column(name = "self_score")
    private Double selfScore;

    @Column(name = "reviewer_score")
    private Double reviewerScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;


    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false)
    private PerformanceReviewStatus status =
            PerformanceReviewStatus.DRAFT;

    @OneToMany(
            mappedBy = "performanceReview",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Kpi> kpis = new ArrayList<>();

    public void addKpi(Kpi kpi) {
        kpis.add(kpi);
        kpi.setPerformanceReview(this);
    }

    public void removeKpi(Kpi kpi) {
        kpis.remove(kpi);
        kpi.setPerformanceReview(null);
    }
}
