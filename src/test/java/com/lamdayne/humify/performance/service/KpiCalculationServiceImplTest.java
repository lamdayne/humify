package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import com.lamdayne.humify.performance.enums.KpiMetricType;
import com.lamdayne.humify.performance.enums.KpiStatus;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.performance.mapper.PerformanceReviewMapper;
import com.lamdayne.humify.performance.repository.PerformanceReviewRepository;
import com.lamdayne.humify.performance.service.impl.KpiCalculationServiceImpl;
import com.lamdayne.humify.task.service.TaskService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiCalculationServiceImpl Tests")
class KpiCalculationServiceImplTest {

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private PerformanceReviewMapper performanceReviewMapper;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private KpiCalculationServiceImpl kpiCalculationService;

    private static final Long COMPANY_ID = 1L;
    private static final Long EMPLOYEE_ID = 10L;
    private static final Long EMPLOYEE_USER_ID = 20L;
    private static final Long REVIEW_ID = 100L;

    private static final LocalDate PERIOD_START =
            LocalDate.of(2026, 8, 1);

    private static final LocalDate PERIOD_END =
            LocalDate.of(2026, 8, 31);

    private Company company;
    private Employee employee;
    private User employeeUser;
    private PerformanceReview review;

    @BeforeEach
    void setUp() {

        company = new Company();
        ReflectionTestUtils.setField(
                company,
                "id",
                COMPANY_ID
        );

        employee = new Employee();
        ReflectionTestUtils.setField(
                employee,
                "id",
                EMPLOYEE_ID
        );

        employeeUser = new User();
        ReflectionTestUtils.setField(
                employeeUser,
                "id",
                EMPLOYEE_USER_ID
        );

        review = new PerformanceReview();

        review.setCompany(company);
        review.setEmployee(employee);
        review.setPeriodStart(PERIOD_START);
        review.setPeriodEnd(PERIOD_END);
        review.setStatus(
                PerformanceReviewStatus.DRAFT
        );

        review.setKpis(
                new ArrayList<>()
        );
    }


    // =====================================================
    // REFRESH REVIEW KPI
    // =====================================================

    @Test
    @DisplayName(
            "Refresh review KPIs successfully"
    )
    void refreshReviewKpis_success() {

        Kpi kpi = createKpi(
                KpiMetricType.TASK_COMPLETION_RATE,
                80.0,
                100.0,
                0.0
        );

        review.addKpi(kpi);

        PerformanceReviewResponse response =
                mock(
                        PerformanceReviewResponse.class
                );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );

        when(
                userService
                        .getUserEntityByEmployeeIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(employeeUser);

        when(
                taskService.countEligibleTasks(
                        EMPLOYEE_USER_ID,
                        PERIOD_START,
                        PERIOD_END
                )
        ).thenReturn(10L);

        when(
                taskService.countCompletedEligibleTasks(
                        EMPLOYEE_USER_ID,
                        PERIOD_START,
                        PERIOD_END
                )
        ).thenReturn(8L);

        when(
                taskService.countOnTimeEligibleTasks(
                        EMPLOYEE_USER_ID,
                        PERIOD_START,
                        PERIOD_END
                )
        ).thenReturn(7L);

        when(
                performanceReviewRepository.save(review)
        ).thenReturn(review);

        when(
                performanceReviewMapper.toResponse(review)
        ).thenReturn(response);


        PerformanceReviewResponse result =
                kpiCalculationService
                        .refreshReviewKpis(
                                userPrincipal,
                                REVIEW_ID
                        );


        assertThat(result)
                .isSameAs(response);

        assertThat(
                kpi.getCurrentValue()
        ).isEqualTo(80.0);

        assertThat(
                kpi.getScore()
        ).isEqualTo(100.0);

        assertThat(
                kpi.getStatus()
        ).isEqualTo(
                KpiStatus.ACHIEVED
        );

        assertThat(
                review.getFinalScore()
        ).isEqualTo(100.0);

        verify(
                performanceReviewRepository
        ).save(review);
    }


    @Test
    @DisplayName(
            "Refresh review KPIs throws exception when review not found"
    )
    void refreshReviewKpis_throwsWhenReviewNotFound() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(() ->
                kpiCalculationService
                        .refreshReviewKpis(
                                userPrincipal,
                                REVIEW_ID
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .PERFORMANCE_REVIEW_NOT_FOUND
                        )
                );

        verify(
                performanceReviewRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Refresh review KPIs throws exception when review is completed"
    )
    void refreshReviewKpis_throwsWhenCompleted() {

        review.setStatus(
                PerformanceReviewStatus.COMPLETED
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );


        assertThatThrownBy(() ->
                kpiCalculationService
                        .refreshReviewKpis(
                                userPrincipal,
                                REVIEW_ID
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .PERFORMANCE_REVIEW_COMPLETED
                        )
                );

        verifyNoInteractions(
                taskService
        );

        verify(
                performanceReviewRepository,
                never()
        ).save(any());
    }


    // =====================================================
    // CALCULATE REVIEW KPI
    // =====================================================

    @Test
    @DisplayName(
            "Calculate task completion KPI successfully"
    )
    void calculateReviewKpis_taskCompletion_success() {

        Kpi kpi = createKpi(
                KpiMetricType.TASK_COMPLETION_RATE,
                80.0,
                50.0,
                0.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                10,
                8,
                6
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isEqualTo(80.0);

        assertThat(
                kpi.getScore()
        ).isEqualTo(50.0);

        assertThat(
                kpi.getStatus()
        ).isEqualTo(
                KpiStatus.ACHIEVED
        );

        assertThat(
                review.getFinalScore()
        ).isEqualTo(50.0);
    }


    @Test
    @DisplayName(
            "Calculate task on-time KPI successfully"
    )
    void calculateReviewKpis_onTime_success() {

        Kpi kpi = createKpi(
                KpiMetricType.TASK_ON_TIME_RATE,
                80.0,
                40.0,
                0.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                10,
                9,
                7
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isEqualTo(70.0);

        assertThat(
                kpi.getScore()
        ).isEqualTo(35.0);

        assertThat(
                kpi.getStatus()
        ).isEqualTo(
                KpiStatus.IN_PROGRESS
        );

        assertThat(
                review.getFinalScore()
        ).isEqualTo(35.0);
    }


    @Test
    @DisplayName(
            "Calculate manual KPI keeps current value"
    )
    void calculateReviewKpis_manual_success() {

        Kpi kpi = createKpi(
                KpiMetricType.MANUAL,
                80.0,
                30.0,
                90.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                100,
                10,
                10
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isEqualTo(90.0);

        assertThat(
                kpi.getScore()
        ).isEqualTo(30.0);

        assertThat(
                kpi.getStatus()
        ).isEqualTo(
                KpiStatus.ACHIEVED
        );
    }


    @Test
    @DisplayName(
            "Manual KPI with null value should use zero"
    )
    void calculateReviewKpis_manualNull_shouldUseZero() {

        Kpi kpi = createKpi(
                KpiMetricType.MANUAL,
                80.0,
                30.0,
                null
        );

        review.addKpi(kpi);

        mockTaskCounts(
                0,
                0,
                0
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isZero();

        assertThat(
                kpi.getScore()
        ).isZero();

        assertThat(
                kpi.getStatus()
        ).isEqualTo(
                KpiStatus.IN_PROGRESS
        );
    }


    @Test
    @DisplayName(
            "Task percentage should be zero when employee has no tasks"
    )
    void calculateReviewKpis_noTasks_shouldReturnZero() {

        Kpi kpi = createKpi(
                KpiMetricType.TASK_COMPLETION_RATE,
                80.0,
                50.0,
                0.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                0,
                0,
                0
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isZero();

        assertThat(
                kpi.getScore()
        ).isZero();

        assertThat(
                review.getFinalScore()
        ).isZero();
    }


    @Test
    @DisplayName(
            "KPI score should not exceed its weight"
    )
    void calculateReviewKpis_scoreShouldNotExceedWeight() {

        Kpi kpi = createKpi(
                KpiMetricType.TASK_COMPLETION_RATE,
                50.0,
                40.0,
                0.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                10,
                10,
                10
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isEqualTo(100.0);

        assertThat(
                kpi.getScore()
        ).isEqualTo(40.0);

        assertThat(
                kpi.getStatus()
        ).isEqualTo(
                KpiStatus.ACHIEVED
        );
    }


    @Test
    @DisplayName(
            "KPI with null metric type should have zero current value"
    )
    void calculateReviewKpis_nullMetricType_shouldReturnZero() {

        Kpi kpi = createKpi(
                null,
                80.0,
                30.0,
                90.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                10,
                8,
                7
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isZero();

        assertThat(
                kpi.getScore()
        ).isZero();
    }


    @Test
    @DisplayName(
            "KPI with invalid target should have zero score"
    )
    void calculateReviewKpis_invalidTarget_shouldReturnZeroScore() {

        Kpi kpi = createKpi(
                KpiMetricType.MANUAL,
                0.0,
                30.0,
                100.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                0,
                0,
                0
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getScore()
        ).isZero();
    }


    @Test
    @DisplayName(
            "KPI with null weight should have zero score"
    )
    void calculateReviewKpis_nullWeight_shouldReturnZeroScore() {

        Kpi kpi = createKpi(
                KpiMetricType.MANUAL,
                80.0,
                null,
                80.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                0,
                0,
                0
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getScore()
        ).isZero();
    }


    @Test
    @DisplayName(
            "Deleted KPI should be ignored"
    )
    void calculateReviewKpis_deletedKpi_shouldBeIgnored() {

        Kpi activeKpi = createKpi(
                KpiMetricType.MANUAL,
                50.0,
                60.0,
                50.0
        );

        Kpi deletedKpi = createKpi(
                KpiMetricType.MANUAL,
                50.0,
                40.0,
                100.0
        );

        deletedKpi.setDeletedAt(
                java.time.Instant.now()
        );

        review.addKpi(activeKpi);
        review.addKpi(deletedKpi);

        mockTaskCounts(
                0,
                0,
                0
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                activeKpi.getScore()
        ).isEqualTo(60.0);

        assertThat(
                review.getFinalScore()
        ).isEqualTo(60.0);

        assertThat(
                deletedKpi.getCurrentValue()
        ).isEqualTo(100.0);
    }


    @Test
    @DisplayName(
            "Final score should equal sum of active KPI scores"
    )
    void calculateReviewKpis_shouldCalculateFinalScore() {

        Kpi kpi1 = createKpi(
                KpiMetricType.TASK_COMPLETION_RATE,
                80.0,
                60.0,
                0.0
        );

        Kpi kpi2 = createKpi(
                KpiMetricType.TASK_ON_TIME_RATE,
                80.0,
                40.0,
                0.0
        );

        review.addKpi(kpi1);
        review.addKpi(kpi2);

        mockTaskCounts(
                10,
                8,
                6
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        // completion = 80%
        // score = 60

        // on-time = 60%
        // achievement = 60 / 80 = 0.75
        // score = 0.75 * 40 = 30

        assertThat(
                review.getFinalScore()
        ).isEqualTo(90.0);
    }


    @Test
    @DisplayName(
            "Percentage should round to two decimal places"
    )
    void calculateReviewKpis_shouldRoundPercentage() {

        Kpi kpi = createKpi(
                KpiMetricType.TASK_COMPLETION_RATE,
                50.0,
                100.0,
                0.0
        );

        review.addKpi(kpi);

        mockTaskCounts(
                3,
                1,
                1
        );


        kpiCalculationService
                .calculateReviewKpis(review);


        assertThat(
                kpi.getCurrentValue()
        ).isEqualTo(33.33);

        assertThat(
                kpi.getScore()
        ).isEqualTo(66.66);
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private Kpi createKpi(
            KpiMetricType metricType,
            Double target,
            Double weight,
            Double currentValue
    ) {

        return Kpi.builder()
                .metricType(metricType)
                .targetValue(target)
                .weight(weight)
                .currentValue(currentValue)
                .score(0.0)
                .status(
                        KpiStatus.IN_PROGRESS
                )
                .build();
    }


    private void mockTaskCounts(
            long total,
            long completed,
            long onTime
    ) {

        when(
                userService
                        .getUserEntityByEmployeeIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                employeeUser
        );

        when(
                taskService.countEligibleTasks(
                        EMPLOYEE_USER_ID,
                        PERIOD_START,
                        PERIOD_END
                )
        ).thenReturn(total);

        when(
                taskService.countCompletedEligibleTasks(
                        EMPLOYEE_USER_ID,
                        PERIOD_START,
                        PERIOD_END
                )
        ).thenReturn(completed);

        when(
                taskService.countOnTimeEligibleTasks(
                        EMPLOYEE_USER_ID,
                        PERIOD_START,
                        PERIOD_END
                )
        ).thenReturn(onTime);
    }
}