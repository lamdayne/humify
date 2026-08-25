package com.lamdayne.humify.performance.repository;

import com.lamdayne.humify.performance.entity.KpiTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KpiTemplateItemRepository
        extends JpaRepository<KpiTemplateItem, Long> {
}
