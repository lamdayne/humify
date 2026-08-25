package com.lamdayne.humify.performance.service.impl;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import com.lamdayne.humify.performance.enums.KpiMetricType;
import com.lamdayne.humify.performance.enums.KpiStatus;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.performance.mapper.PerformanceReviewMapper;
import com.lamdayne.humify.performance.repository.PerformanceReviewRepository;
import com.lamdayne.humify.performance.service.KpiCalculationService;
import com.lamdayne.humify.task.service.TaskService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiCalculationServiceImpl
        implements KpiCalculationService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final PerformanceReviewMapper performanceReviewMapper;

    private final TaskService taskService;
    private final UserService userService;

    @Override
    @Transactional
    public PerformanceReviewResponse refreshReviewKpis(
            UserPrincipal userPrincipal,
            Long reviewId
    ) {
        Long companyId = userPrincipal.getCompanyId();

        PerformanceReview review = performanceReviewRepository
                .findByIdAndCompany_IdAndDeletedAtIsNull(
                        reviewId,
                        companyId
                )
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.PERFORMANCE_REVIEW_NOT_FOUND
                        )
                );

        /*
         * Review đã COMPLETED thì không cho tính lại,
         * vì dữ liệu lịch sử phải được giữ nguyên.
         */
        if (review.getStatus() == PerformanceReviewStatus.COMPLETED) {
            throw new AppException(
                    ErrorCode.PERFORMANCE_REVIEW_COMPLETED
            );
        }

        calculateReviewKpis(review);
        PerformanceReview saved =
                performanceReviewRepository.save(review);

        return performanceReviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void calculateReviewKpis(
            PerformanceReview review
    ) {

        Long companyId = review.getCompany().getId();
        Long employeeId = review.getEmployee().getId();

        User employeeUser =
                userService.getUserEntityByEmployeeIdAndCompanyId(
                        employeeId,
                        companyId
                );

        long totalTasks =
                taskService.countEligibleTasks(
                        employeeUser.getId(),
                        review.getPeriodStart(),
                        review.getPeriodEnd()
                );

        long completedTasks =
                taskService.countCompletedEligibleTasks(
                        employeeUser.getId(),
                        review.getPeriodStart(),
                        review.getPeriodEnd()
                );

        long onTimeTasks =
                taskService.countOnTimeEligibleTasks(
                        employeeUser.getId(),
                        review.getPeriodStart(),
                        review.getPeriodEnd()
                );

        for (Kpi kpi : review.getKpis()) {

            if (kpi.getDeletedAt() != null) {
                continue;
            }

            double currentValue =
                    calculateCurrentValue(
                            kpi.getMetricType(),
                            totalTasks,
                            completedTasks,
                            onTimeTasks,
                            kpi.getCurrentValue()
                    );

            kpi.setCurrentValue(currentValue);

            double score =
                    calculateScore(
                            currentValue,
                            kpi.getTargetValue(),
                            kpi.getWeight()
                    );

            kpi.setScore(score);

            /*
             * Khi chưa complete:
             * đạt target -> ACHIEVED
             * chưa đạt -> IN_PROGRESS
             */
            if (currentValue >= kpi.getTargetValue()) {
                kpi.setStatus(KpiStatus.ACHIEVED);
            } else {
                kpi.setStatus(KpiStatus.IN_PROGRESS);
            }
        }

        double finalScore =
                review.getKpis()
                        .stream()
                        .filter(kpi -> kpi.getDeletedAt() == null)
                        .mapToDouble(kpi ->
                                kpi.getScore() == null
                                        ? 0.0
                                        : kpi.getScore()
                        )
                        .sum();

        review.setFinalScore(
                round(finalScore)
        );
    }

    private double calculateCurrentValue(
            KpiMetricType metricType,
            long totalTasks,
            long completedTasks,
            long onTimeTasks,
            Double manualValue
    ) {
        if (metricType == null) {
            return 0.0;
        }

        return switch (metricType) {

            case TASK_COMPLETION_RATE ->
                    percentage(
                            completedTasks,
                            totalTasks
                    );

            case TASK_ON_TIME_RATE ->
                    percentage(
                            onTimeTasks,
                            totalTasks
                    );

            case MANUAL ->
                    manualValue == null
                            ? 0.0
                            : manualValue;
        };
    }

    private double percentage(
            long value,
            long total
    ) {
        if (total == 0) {
            return 0.0;
        }

        return round(
                value * 100.0 / total
        );
    }

    private double calculateScore(
            double currentValue,
            Double targetValue,
            Double weight
    ) {
        if (targetValue == null ||
                targetValue <= 0 ||
                weight == null) {
            return 0.0;
        }

        double achievement =
                currentValue / targetValue;

        /*
         * Không cho vượt quá weight.
         */
        achievement = Math.min(
                achievement,
                1.0
        );

        return round(
                achievement * weight
        );
    }

    private void updateStatus(Kpi kpi) {

        if (kpi.getCurrentValue() >= kpi.getTargetValue()) {

            kpi.setStatus(
                    KpiStatus.ACHIEVED
            );

        } else {

            kpi.setStatus(
                    KpiStatus.IN_PROGRESS
            );
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}