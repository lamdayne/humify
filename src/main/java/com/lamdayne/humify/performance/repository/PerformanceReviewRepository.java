package com.lamdayne.humify.performance.repository;

import com.lamdayne.humify.performance.entity.PerformanceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
}
