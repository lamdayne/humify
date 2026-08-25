package com.lamdayne.humify.performance.entity;

import com.lamdayne.humify.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

    @Entity
    @Table(name = "kpi_templates")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class KpiTemplate extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "company_id", nullable = false)
        private Long companyId;

        @Column(nullable = false)
        private String name;
        @Column(columnDefinition = "TEXT")
        private String description;

        @Column(name = "is_active", nullable = false)
        private Boolean isActive = true;

        @Column(name = "created_by", nullable = false)
        private Long createdBy;

        @OneToMany(
                mappedBy = "template",
                cascade = CascadeType.ALL,
                orphanRemoval = true
        )
        private List<KpiTemplateItem> items = new ArrayList<>();

        public void addItem(KpiTemplateItem item) {
            items.add(item);
            item.setTemplate(this);
        }

        public void removeItem(KpiTemplateItem item) {
            items.remove(item);
            item.setTemplate(null);
        }

    }