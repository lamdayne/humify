package com.lamdayne.humify.performance.repository;

import com.lamdayne.humify.performance.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    boolean existsByEmployeeIdAndReviewPeriod(Long employeeId, String reviewPeriod);
    List<PerformanceReview> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
