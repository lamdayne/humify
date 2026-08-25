package com.lamdayne.humify.performance.entity;
import com.lamdayne.humify.common.base.BaseEntity;
import com.lamdayne.humify.performance.enums.KpiMetricType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "kpi_template_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KpiTemplateItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "template_id",
            nullable = false
    )
    private KpiTemplate template;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "metric_type",
            nullable = false,
            columnDefinition = "kpi_metric_type"
    )
    private KpiMetricType metricType;

    @Column(name = "target_value", nullable = false)
    private Double targetValue;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Double weight;


}