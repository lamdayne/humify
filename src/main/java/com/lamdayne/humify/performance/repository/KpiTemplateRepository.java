package com.lamdayne.humify.performance.repository;

import com.lamdayne.humify.performance.entity.KpiTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KpiTemplateRepository
        extends JpaRepository<KpiTemplate, Long> {
    List<KpiTemplate> findAllByCompanyIdAndDeletedAtIsNull(
            Long companyId
    );

    Optional<KpiTemplate> findByIdAndCompanyIdAndDeletedAtIsNull(
            Long id,
            Long companyId
    );

    boolean existsByCompanyIdAndNameIgnoreCaseAndDeletedAtIsNull(
            Long companyId,
            String name
    );

    boolean existsByCompanyIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
            Long companyId,
            String name,
            Long id
    );
}
