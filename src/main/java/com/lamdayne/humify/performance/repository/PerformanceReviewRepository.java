package com.lamdayne.humify.performance.repository;

import com.lamdayne.humify.performance.entity.PerformanceReview;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long>, JpaSpecificationExecutor<PerformanceReview> {

    Optional<PerformanceReview>
    findByIdAndCompany_IdAndDeletedAtIsNull(
            Long id,
            Long companyId
    );

    List<PerformanceReview>
    findAllByCompany_IdAndDeletedAtIsNull(
            Long companyId
    );

    boolean existsByCompany_IdAndEmployee_IdAndPeriodStartAndPeriodEndAndDeletedAtIsNull(
            Long companyId,
            Long employeeId,
            java.time.LocalDate periodStart,
            java.time.LocalDate periodEnd
    );

    long countByCompany_IdAndReviewer_IdAndDeletedAtIsNull(
            Long companyId,
            Long reviewerId
    );

    long countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
            Long companyId,
            Long reviewerId,
            PerformanceReviewStatus status
    );
}
