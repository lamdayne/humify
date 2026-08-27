package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.service.CompanyService;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.service.EmployeeService;
import com.lamdayne.humify.performance.dto.request.CreatePerformanceReviewRequest;
import com.lamdayne.humify.performance.dto.request.ManagerReviewRequest;
import com.lamdayne.humify.performance.dto.request.SelfReviewRequest;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewResponse;
import com.lamdayne.humify.performance.dto.response.PerformanceReviewSummaryResponse;
import com.lamdayne.humify.performance.entity.Kpi;
import com.lamdayne.humify.performance.entity.KpiTemplate;
import com.lamdayne.humify.performance.entity.KpiTemplateItem;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import com.lamdayne.humify.performance.enums.KpiMetricType;
import com.lamdayne.humify.performance.enums.KpiStatus;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.performance.mapper.PerformanceReviewMapper;
import com.lamdayne.humify.performance.repository.KpiTemplateRepository;
import com.lamdayne.humify.performance.repository.PerformanceReviewRepository;
import com.lamdayne.humify.performance.service.impl.PerformanceReviewServiceImpl;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceReviewServiceImpl Tests")
class PerformanceReviewServiceImplTest {

    // =====================================================
    // MOCKS
    // =====================================================

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private KpiTemplateRepository kpiTemplateRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private UserService userService;

    @Mock
    private CompanyService companyService;

    @Mock
    private KpiCalculationService kpiCalculationService;

    @Mock
    private PerformanceReviewMapper performanceReviewMapper;

    @Mock
    private UserPrincipal userPrincipal;


    @InjectMocks
    private PerformanceReviewServiceImpl performanceReviewService;


    // =====================================================
    // CONSTANTS
    // =====================================================

    private static final Long COMPANY_ID = 1L;

    private static final Long EMPLOYEE_ID = 2L;

    private static final Long REVIEWER_ID = 10L;

    private static final Long EMPLOYEE_USER_ID = 20L;

    private static final Long TEMPLATE_ID = 100L;

    private static final Long REVIEW_ID = 200L;

    private static final LocalDate PERIOD_START =
            LocalDate.of(2026, 8, 1);

    private static final LocalDate PERIOD_END =
            LocalDate.of(2026, 8, 31);


    // =====================================================
    // TEST DATA
    // =====================================================

    private Company company;

    private Employee employee;

    private User reviewer;

    private User employeeUser;

    private KpiTemplate template;

    private KpiTemplateItem completionItem;

    private KpiTemplateItem onTimeItem;

    private PerformanceReview review;

    private PerformanceReviewResponse reviewResponse;

    private CreatePerformanceReviewRequest createRequest;


    // =====================================================
    // SETUP
    // =====================================================

    @BeforeEach
    void setUp() {

        // =================================================
        // COMPANY
        // =================================================

        company = new Company();

        ReflectionTestUtils.setField(
                company,
                "id",
                COMPANY_ID
        );


        // =================================================
        // EMPLOYEE
        // =================================================

        employee = new Employee();

        ReflectionTestUtils.setField(
                employee,
                "id",
                EMPLOYEE_ID
        );

        employee.setFullName(
                "Tran Hoai Hang"
        );


        // =================================================
        // REVIEWER
        // =================================================

        reviewer = new User();

        ReflectionTestUtils.setField(
                reviewer,
                "id",
                REVIEWER_ID
        );

        reviewer.setEmail(
                "manager@company.com"
        );

        reviewer.setActive(true);


        // =================================================
        // EMPLOYEE USER
        // =================================================

        employeeUser = new User();

        ReflectionTestUtils.setField(
                employeeUser,
                "id",
                EMPLOYEE_USER_ID
        );

        employeeUser.setEmployee(employee);

        employeeUser.setEmail(
                "employee@company.com"
        );

        employeeUser.setActive(true);


        // =================================================
        // KPI TEMPLATE ITEM 1
        // =================================================

        completionItem =
                new KpiTemplateItem();

        ReflectionTestUtils.setField(
                completionItem,
                "id",
                1001L
        );

        completionItem.setTitle(
                "Task Completion Rate"
        );

        completionItem.setDescription(
                "Percentage of completed tasks"
        );

        completionItem.setMetricType(
                KpiMetricType.TASK_COMPLETION_RATE
        );

        completionItem.setTargetValue(
                80.0
        );

        completionItem.setUnit("%");

        completionItem.setWeight(
                50.0
        );


        // =================================================
        // KPI TEMPLATE ITEM 2
        // =================================================

        onTimeItem =
                new KpiTemplateItem();

        ReflectionTestUtils.setField(
                onTimeItem,
                "id",
                1002L
        );

        onTimeItem.setTitle(
                "On-time Delivery Rate"
        );

        onTimeItem.setDescription(
                "Percentage of tasks completed on time"
        );

        onTimeItem.setMetricType(
                KpiMetricType.TASK_ON_TIME_RATE
        );

        onTimeItem.setTargetValue(
                90.0
        );

        onTimeItem.setUnit("%");

        onTimeItem.setWeight(
                50.0
        );


        // =================================================
        // KPI TEMPLATE
        // =================================================

        template = new KpiTemplate();

        ReflectionTestUtils.setField(
                template,
                "id",
                TEMPLATE_ID
        );

        template.setName(
                "Developer KPI"
        );

        template.setIsActive(true);

        template.setItems(
                new ArrayList<>(
                        List.of(
                                completionItem,
                                onTimeItem
                        )
                )
        );


        // =================================================
        // CREATE REQUEST
        // =================================================

        createRequest =
                new CreatePerformanceReviewRequest();

        createRequest.setEmployeeId(
                EMPLOYEE_ID
        );

        createRequest.setReviewerId(
                REVIEWER_ID
        );

        createRequest.setTemplateId(
                TEMPLATE_ID
        );

        createRequest.setPeriodStart(
                PERIOD_START
        );

        createRequest.setPeriodEnd(
                PERIOD_END
        );


        // =================================================
        // REVIEW
        // =================================================

        review = new PerformanceReview();

        ReflectionTestUtils.setField(
                review,
                "id",
                REVIEW_ID
        );

        review.setCompany(company);
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setTemplate(template);

        review.setPeriodStart(
                PERIOD_START
        );

        review.setPeriodEnd(
                PERIOD_END
        );

        review.setStatus(
                PerformanceReviewStatus.DRAFT
        );


        // =================================================
        // RESPONSE
        // =================================================

        reviewResponse =
                new PerformanceReviewResponse();

        reviewResponse.setId(
                REVIEW_ID
        );

        reviewResponse.setEmployeeId(
                EMPLOYEE_ID
        );

        reviewResponse.setReviewerId(
                REVIEWER_ID
        );

        reviewResponse.setPeriodStart(
                PERIOD_START
        );

        reviewResponse.setPeriodEnd(
                PERIOD_END
        );

        reviewResponse.setStatus(
                PerformanceReviewStatus.DRAFT
        );
    }


    // =====================================================
    // CREATE REVIEW
    // =====================================================

    @Test
    @DisplayName("Create performance review successfully")
    void createReview_success() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                companyService.getCompanyById(
                        COMPANY_ID
                )
        ).thenReturn(company);

        when(
                employeeService
                        .getEmployeeEntityByIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(employee);

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                REVIEWER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(reviewer);

        when(
                userService.canBePerformanceReviewer(
                        REVIEWER_ID
                )
        ).thenReturn(true);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                performanceReviewRepository
                        .existsByCompany_IdAndEmployee_IdAndPeriodStartAndPeriodEndAndDeletedAtIsNull(
                                COMPANY_ID,
                                EMPLOYEE_ID,
                                PERIOD_START,
                                PERIOD_END
                        )
        ).thenReturn(false);

        when(
                performanceReviewRepository.save(
                        any(PerformanceReview.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                performanceReviewMapper.toResponse(
                        any(PerformanceReview.class)
                )
        ).thenReturn(
                reviewResponse
        );


        PerformanceReviewResponse result =
                performanceReviewService.createReview(
                        userPrincipal,
                        createRequest
                );


        assertThat(result)
                .isNotNull();

        assertThat(result.getEmployeeId())
                .isEqualTo(EMPLOYEE_ID);

        assertThat(result.getReviewerId())
                .isEqualTo(REVIEWER_ID);

        assertThat(result.getStatus())
                .isEqualTo(
                        PerformanceReviewStatus.DRAFT
                );


        verify(
                performanceReviewRepository,
                times(1)
        ).save(
                any(PerformanceReview.class)
        );
    }


    @Test
    @DisplayName(
            "Create review initializes status, scores and KPIs correctly"
    )
    void createReview_shouldInitializeReviewCorrectly() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                companyService.getCompanyById(
                        COMPANY_ID
                )
        ).thenReturn(company);

        when(
                employeeService
                        .getEmployeeEntityByIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(employee);

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                REVIEWER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(reviewer);

        when(
                userService.canBePerformanceReviewer(
                        REVIEWER_ID
                )
        ).thenReturn(true);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                performanceReviewRepository
                        .existsByCompany_IdAndEmployee_IdAndPeriodStartAndPeriodEndAndDeletedAtIsNull(
                                COMPANY_ID,
                                EMPLOYEE_ID,
                                PERIOD_START,
                                PERIOD_END
                        )
        ).thenReturn(false);

        when(
                performanceReviewRepository.save(
                        any(PerformanceReview.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                performanceReviewMapper.toResponse(
                        any()
                )
        ).thenReturn(
                reviewResponse
        );


        ArgumentCaptor<PerformanceReview> captor =
                ArgumentCaptor.forClass(
                        PerformanceReview.class
                );


        performanceReviewService.createReview(
                userPrincipal,
                createRequest
        );


        verify(
                performanceReviewRepository
        ).save(
                captor.capture()
        );


        PerformanceReview savedReview =
                captor.getValue();


        assertThat(savedReview.getCompany())
                .isEqualTo(company);

        assertThat(savedReview.getEmployee())
                .isEqualTo(employee);

        assertThat(savedReview.getReviewer())
                .isEqualTo(reviewer);

        assertThat(savedReview.getTemplate())
                .isEqualTo(template);


        assertThat(savedReview.getStatus())
                .isEqualTo(
                        PerformanceReviewStatus.DRAFT
                );


        assertThat(savedReview.getSelfScore())
                .isNull();

        assertThat(savedReview.getReviewerScore())
                .isNull();

        assertThat(savedReview.getFinalScore())
                .isNull();

        assertThat(savedReview.getFeedback())
                .isNull();


        assertThat(savedReview.getKpis())
                .hasSize(2);


        Kpi firstKpi =
                savedReview.getKpis().get(0);


        assertThat(firstKpi.getEmployee())
                .isEqualTo(employee);

        assertThat(firstKpi.getCompany())
                .isEqualTo(company);

        assertThat(firstKpi.getCurrentValue())
                .isEqualTo(0.0);

        assertThat(firstKpi.getScore())
                .isEqualTo(0.0);

        assertThat(firstKpi.getStatus())
                .isEqualTo(
                        KpiStatus.IN_PROGRESS
                );

        assertThat(firstKpi.getStartDate())
                .isEqualTo(PERIOD_START);

        assertThat(firstKpi.getEndDate())
                .isEqualTo(PERIOD_END);
    }


    @Test
    @DisplayName(
            "Create review ignores deleted KPI template items"
    )
    void createReview_shouldIgnoreDeletedTemplateItems() {

        ReflectionTestUtils.setField(
                onTimeItem,
                "deletedAt",
                Instant.now()
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                companyService.getCompanyById(
                        COMPANY_ID
                )
        ).thenReturn(company);

        when(
                employeeService
                        .getEmployeeEntityByIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(employee);

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                REVIEWER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(reviewer);

        when(
                userService.canBePerformanceReviewer(
                        REVIEWER_ID
                )
        ).thenReturn(true);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                performanceReviewRepository
                        .existsByCompany_IdAndEmployee_IdAndPeriodStartAndPeriodEndAndDeletedAtIsNull(
                                COMPANY_ID,
                                EMPLOYEE_ID,
                                PERIOD_START,
                                PERIOD_END
                        )
        ).thenReturn(false);

        when(
                performanceReviewRepository.save(
                        any()
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                performanceReviewMapper.toResponse(
                        any()
                )
        ).thenReturn(
                reviewResponse
        );


        ArgumentCaptor<PerformanceReview> captor =
                ArgumentCaptor.forClass(
                        PerformanceReview.class
                );


        performanceReviewService.createReview(
                userPrincipal,
                createRequest
        );


        verify(
                performanceReviewRepository
        ).save(
                captor.capture()
        );


        assertThat(
                captor.getValue().getKpis()
        ).hasSize(1);


        assertThat(
                captor.getValue()
                        .getKpis()
                        .get(0)
                        .getTitle()
        ).isEqualTo(
                "Task Completion Rate"
        );
    }


    @Test
    @DisplayName(
            "Create review throws exception when period is missing"
    )
    void createReview_throwsWhenPeriodRequired() {

        createRequest.setPeriodStart(null);


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                        .PERFORMANCE_REVIEW_PERIOD_REQUIRED
                        )
                );


        verifyNoInteractions(
                companyService,
                employeeService,
                userService,
                kpiTemplateRepository
        );
    }


    @Test
    @DisplayName(
            "Create review throws exception when period is invalid"
    )
    void createReview_throwsWhenPeriodInvalid() {

        createRequest.setPeriodStart(
                LocalDate.of(
                        2026,
                        8,
                        31
                )
        );

        createRequest.setPeriodEnd(
                LocalDate.of(
                        2026,
                        8,
                        1
                )
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                        .PERFORMANCE_REVIEW_PERIOD_INVALID
                        )
                );


        verify(
                performanceReviewRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Create review throws exception when reviewer is invalid"
    )
    void createReview_throwsWhenReviewerInvalid() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                companyService.getCompanyById(
                        COMPANY_ID
                )
        ).thenReturn(company);

        when(
                employeeService
                        .getEmployeeEntityByIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(employee);

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                REVIEWER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(reviewer);

        when(
                userService.canBePerformanceReviewer(
                        REVIEWER_ID
                )
        ).thenReturn(false);


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                        .PERFORMANCE_REVIEW_REVIEWER_INVALID
                        )
                );


        verify(
                performanceReviewRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Create review throws exception when KPI template is not found"
    )
    void createReview_throwsWhenTemplateNotFound() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                companyService.getCompanyById(
                        COMPANY_ID
                )
        ).thenReturn(company);

        when(
                employeeService
                        .getEmployeeEntityByIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(employee);

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                REVIEWER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(reviewer);

        when(
                userService.canBePerformanceReviewer(
                        REVIEWER_ID
                )
        ).thenReturn(true);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                ErrorCode.KPI_TEMPLATE_NOT_FOUND
                        )
                );
    }


    @Test
    @DisplayName(
            "Create review throws exception when KPI template is inactive"
    )
    void createReview_throwsWhenTemplateInactive() {

        template.setIsActive(false);


        mockValidCreateDependencies();


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                ErrorCode.KPI_TEMPLATE_INACTIVE
                        )
                );


        verify(
                performanceReviewRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Create review throws exception when template has no active KPI items"
    )
    void createReview_throwsWhenTemplateItemsEmpty() {

        ReflectionTestUtils.setField(
                completionItem,
                "deletedAt",
                Instant.now()
        );

        ReflectionTestUtils.setField(
                onTimeItem,
                "deletedAt",
                Instant.now()
        );


        mockValidCreateDependencies();


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                        .KPI_TEMPLATE_ITEMS_EMPTY
                        )
                );
    }


    @Test
    @DisplayName(
            "Create review throws exception when review already exists"
    )
    void createReview_throwsWhenDuplicate() {

        mockValidCreateDependencies();


        when(
                performanceReviewRepository
                        .existsByCompany_IdAndEmployee_IdAndPeriodStartAndPeriodEndAndDeletedAtIsNull(
                                COMPANY_ID,
                                EMPLOYEE_ID,
                                PERIOD_START,
                                PERIOD_END
                        )
        ).thenReturn(true);


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .createReview(
                                        userPrincipal,
                                        createRequest
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
                                        .PERFORMANCE_REVIEW_EXISTED
                        )
                );


        verify(
                performanceReviewRepository,
                never()
        ).save(any());
    }


    // =====================================================
    // GET REVIEW BY ID
    // =====================================================

    @Test
    @DisplayName(
            "Get review by ID successfully"
    )
    void getReviewById_success() {

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
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PerformanceReviewResponse result =
                performanceReviewService
                        .getReviewById(
                                userPrincipal,
                                REVIEW_ID
                        );


        assertThat(result)
                .isEqualTo(reviewResponse);
    }


    @Test
    @DisplayName(
            "Get review by ID throws exception when not found"
    )
    void getReviewById_throwsWhenNotFound() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                999L,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .getReviewById(
                                        userPrincipal,
                                        999L
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
    }


    // =====================================================
    // GET REVIEWS
    // =====================================================


    @Test
    @DisplayName("Get reviews successfully")
    void getReviews_success() {

        Page<PerformanceReview> reviewPage =
                new PageImpl<>(
                        List.of(review),
                        PageRequest.of(0, 10),
                        1
                );

        when(
                performanceReviewRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(reviewPage);

        when(
                performanceReviewMapper.toResponse(review)
        ).thenReturn(reviewResponse);


        PageResponse<PerformanceReviewResponse> result =
                performanceReviewService.getReviews(
                        userPrincipal,
                        EMPLOYEE_ID,
                        PerformanceReviewStatus.DRAFT,
                        PERIOD_START,
                        PERIOD_END,
                        0,
                        10,
                        "id,desc"
                );


        assertThat(result).isNotNull();

        assertThat(result.getItems())
                .hasSize(1);

        assertThat(result.getItems().get(0))
                .isEqualTo(reviewResponse);

        assertThat(result.getTotalElements())
                .isEqualTo(1);

        assertThat(result.getTotalPages())
                .isEqualTo(1);

        assertThat(result.getPageNo())
                .isEqualTo(0);

        assertThat(result.getPageSize())
                .isEqualTo(10);


        verify(
                performanceReviewRepository
        ).findAll(
                any(Specification.class),
                any(Pageable.class)
        );

        verify(
                performanceReviewMapper
        ).toResponse(review);
    }


    @Test
    @DisplayName("Get reviews returns empty page")
    void getReviews_empty() {

        Page<PerformanceReview> reviewPage =
                new PageImpl<>(
                        Collections.emptyList(),
                        PageRequest.of(0, 10),
                        0
                );

        when(
                performanceReviewRepository.findAll(
                        any(Specification.class),
                        any(Pageable.class)
                )
        ).thenReturn(reviewPage);


        PageResponse<PerformanceReviewResponse> result =
                performanceReviewService.getReviews(
                        userPrincipal,
                        null,
                        null,
                        null,
                        null,
                        0,
                        10
                );


        assertThat(result).isNotNull();

        assertThat(result.getItems())
                .isEmpty();

        assertThat(result.getTotalElements())
                .isZero();

        assertThat(result.getTotalPages())
                .isZero();


        verify(
                performanceReviewRepository
        ).findAll(
                any(Specification.class),
                any(Pageable.class)
        );

        verifyNoInteractions(
                performanceReviewMapper
        );
    }

    @Test
    @DisplayName(
            "Get reviews throws exception when filter period is invalid"
    )
    void getReviews_throwsWhenPeriodInvalid() {

        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .getReviews(
                                        userPrincipal,
                                        null,
                                        null,
                                        LocalDate.of(
                                                2026,
                                                8,
                                                31
                                        ),
                                        LocalDate.of(
                                                2026,
                                                8,
                                                1
                                        ),
                                        0,
                                        10
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
                                        .PERFORMANCE_REVIEW_PERIOD_INVALID
                        )
                );


        verify(
                performanceReviewRepository,
                never()
        ).findAll(
                any(Specification.class),
                any(org.springframework.data.domain.Pageable.class)
        );
    }


    // =====================================================
    // GET MY REVIEWS
    // =====================================================

    @Test
    @DisplayName(
            "Get my performance reviews successfully"
    )
    void getMyReviews_success() {

        when(
                userPrincipal.getId()
        ).thenReturn(
                EMPLOYEE_USER_ID
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                EMPLOYEE_USER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                employeeUser
        );


        Page<PerformanceReview> pageData =
                new PageImpl<>(
                        List.of(review)
                );


        when(
                performanceReviewRepository.findAll(
                        any(Specification.class),
                        any(org.springframework.data.domain.Pageable.class)
                )
        ).thenReturn(
                pageData
        );


        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PageResponse<PerformanceReviewResponse> result =
                performanceReviewService.getMyReviews(
                        userPrincipal,
                        null,
                        null,
                        null,
                        0,
                        10,
                        "id,desc"
                );


        assertThat(result.getItems())
                .hasSize(1);

        assertThat(result.getTotalElements())
                .isEqualTo(1);
    }


    @Test
    @DisplayName(
            "Get my reviews throws exception when user has no employee"
    )
    void getMyReviews_throwsWhenEmployeeNotFound() {

        User userWithoutEmployee =
                new User();

        ReflectionTestUtils.setField(
                userWithoutEmployee,
                "id",
                EMPLOYEE_USER_ID
        );


        when(
                userPrincipal.getId()
        ).thenReturn(
                EMPLOYEE_USER_ID
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                EMPLOYEE_USER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                userWithoutEmployee
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .getMyReviews(
                                        userPrincipal,
                                        null,
                                        null,
                                        null,
                                        0,
                                        10
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
                                ErrorCode.EMPLOYEE_NOT_FOUND
                        )
                );
    }


    @Test
    @DisplayName(
            "Get my reviews throws exception when filter period is invalid"
    )
    void getMyReviews_throwsWhenPeriodInvalid() {

        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .getMyReviews(
                                        userPrincipal,
                                        null,
                                        LocalDate.of(
                                                2026,
                                                8,
                                                31
                                        ),
                                        LocalDate.of(
                                                2026,
                                                8,
                                                1
                                        ),
                                        0,
                                        10
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
                                        .PERFORMANCE_REVIEW_PERIOD_INVALID
                        )
                );
    }


    // =====================================================
    // GET MY ASSIGNED REVIEWS
    // =====================================================

    @Test
    @DisplayName(
            "Get reviews assigned to reviewer successfully"
    )
    void getMyAssignedReviews_success() {

        when(
                userPrincipal.getId()
        ).thenReturn(
                REVIEWER_ID
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );


        Page<PerformanceReview> pageData =
                new PageImpl<>(
                        List.of(review)
                );


        when(
                performanceReviewRepository.findAll(
                        any(Specification.class),
                        any(org.springframework.data.domain.Pageable.class)
                )
        ).thenReturn(
                pageData
        );


        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PageResponse<PerformanceReviewResponse> result =
                performanceReviewService
                        .getMyAssignedReviews(
                                userPrincipal,
                                EMPLOYEE_ID,
                                null,
                                null,
                                null,
                                0,
                                10,
                                "id,desc"
                        );


        assertThat(result)
                .isNotNull();

        assertThat(result.getItems())
                .hasSize(1);
    }


    @Test
    @DisplayName(
            "Get assigned reviews throws exception when period is invalid"
    )
    void getMyAssignedReviews_throwsWhenPeriodInvalid() {

        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .getMyAssignedReviews(
                                        userPrincipal,
                                        null,
                                        null,
                                        LocalDate.of(
                                                2026,
                                                9,
                                                1
                                        ),
                                        LocalDate.of(
                                                2026,
                                                8,
                                                1
                                        ),
                                        0,
                                        10
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
                                        .PERFORMANCE_REVIEW_PERIOD_INVALID
                        )
                );
    }


    // =====================================================
    // SELF REVIEW
    // =====================================================

    @Test
    @DisplayName(
            "Employee submits self review successfully"
    )
    void selfReview_success() {

        SelfReviewRequest request =
                new SelfReviewRequest();

        request.setSelfScore(
                85.0
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                EMPLOYEE_USER_ID
        );

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
                        .getUserEntityByIdAndCompanyId(
                                EMPLOYEE_USER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                employeeUser
        );

        when(
                performanceReviewRepository.save(
                        review
                )
        ).thenReturn(
                review
        );

        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PerformanceReviewResponse result =
                performanceReviewService.selfReview(
                        userPrincipal,
                        REVIEW_ID,
                        request
                );


        assertThat(result)
                .isNotNull();

        assertThat(review.getSelfScore())
                .isEqualTo(85.0);

        assertThat(review.getStatus())
                .isEqualTo(
                        PerformanceReviewStatus.SELF_REVIEW
                );


        verify(
                performanceReviewRepository
        ).save(review);
    }


    @Test
    @DisplayName(
            "Self review throws exception when review status is not DRAFT"
    )
    void selfReview_throwsWhenStatusInvalid() {

        review.setStatus(
                PerformanceReviewStatus.SELF_REVIEW
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );


        SelfReviewRequest request =
                new SelfReviewRequest();

        request.setSelfScore(
                80.0
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .selfReview(
                                        userPrincipal,
                                        REVIEW_ID,
                                        request
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
                                        .PERFORMANCE_REVIEW_INVALID_STATUS
                        )
                );
    }


    @Test
    @DisplayName(
            "Self review throws exception when current user is not reviewed employee"
    )
    void selfReview_throwsWhenForbidden() {

        Employee anotherEmployee =
                new Employee();

        ReflectionTestUtils.setField(
                anotherEmployee,
                "id",
                999L
        );


        User anotherUser =
                new User();

        ReflectionTestUtils.setField(
                anotherUser,
                "id",
                EMPLOYEE_USER_ID
        );

        anotherUser.setEmployee(
                anotherEmployee
        );


        when(
                userPrincipal.getId()
        ).thenReturn(
                EMPLOYEE_USER_ID
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

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
                        .getUserEntityByIdAndCompanyId(
                                EMPLOYEE_USER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                anotherUser
        );


        SelfReviewRequest request =
                new SelfReviewRequest();

        request.setSelfScore(
                80.0
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .selfReview(
                                        userPrincipal,
                                        REVIEW_ID,
                                        request
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
                                        .PERFORMANCE_REVIEW_SELF_REVIEW_FORBIDDEN
                        )
                );
    }


    @Test
    @DisplayName(
            "Self review throws exception when current user has no employee"
    )
    void selfReview_throwsWhenUserHasNoEmployee() {

        User userWithoutEmployee =
                new User();

        ReflectionTestUtils.setField(
                userWithoutEmployee,
                "id",
                EMPLOYEE_USER_ID
        );


        when(
                userPrincipal.getId()
        ).thenReturn(
                EMPLOYEE_USER_ID
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

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
                        .getUserEntityByIdAndCompanyId(
                                EMPLOYEE_USER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                userWithoutEmployee
        );


        SelfReviewRequest request =
                new SelfReviewRequest();

        request.setSelfScore(
                80.0
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .selfReview(
                                        userPrincipal,
                                        REVIEW_ID,
                                        request
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
                                        .PERFORMANCE_REVIEW_SELF_REVIEW_FORBIDDEN
                        )
                );
    }


    // =====================================================
    // MANAGER REVIEW
    // =====================================================

    @Test
    @DisplayName(
            "Assigned reviewer submits manager review successfully"
    )
    void managerReview_success() {

        review.setStatus(
                PerformanceReviewStatus.SELF_REVIEW
        );


        ManagerReviewRequest request =
                new ManagerReviewRequest();

        request.setReviewerScore(
                90.0
        );

        request.setFeedback(
                "Good performance"
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                REVIEWER_ID
        );

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
                performanceReviewRepository.save(
                        review
                )
        ).thenReturn(
                review
        );

        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PerformanceReviewResponse result =
                performanceReviewService.managerReview(
                        userPrincipal,
                        REVIEW_ID,
                        request
                );


        assertThat(result)
                .isNotNull();

        assertThat(review.getReviewerScore())
                .isEqualTo(90.0);

        assertThat(review.getFeedback())
                .isEqualTo(
                        "Good performance"
                );

        assertThat(review.getStatus())
                .isEqualTo(
                        PerformanceReviewStatus.MANAGER_REVIEW
                );
    }


    @Test
    @DisplayName(
            "Manager review throws exception when review status is invalid"
    )
    void managerReview_throwsWhenStatusInvalid() {

        review.setStatus(
                PerformanceReviewStatus.DRAFT
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .managerReview(
                                        userPrincipal,
                                        REVIEW_ID,
                                        new ManagerReviewRequest()
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
                                        .PERFORMANCE_REVIEW_INVALID_STATUS
                        )
                );
    }


    @Test
    @DisplayName(
            "Manager review throws exception when user is not assigned reviewer"
    )
    void managerReview_throwsWhenReviewerForbidden() {

        review.setStatus(
                PerformanceReviewStatus.SELF_REVIEW
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                999L
        );

        when(
                userPrincipal.getAuthorities()
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .managerReview(
                                        userPrincipal,
                                        REVIEW_ID,
                                        new ManagerReviewRequest()
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
                                        .PERFORMANCE_REVIEW_REVIEWER_FORBIDDEN
                        )
                );
    }

    @Test
    @DisplayName(
            "FULL_ACCESS user can submit manager review even when not assigned reviewer"
    )
    void managerReview_successWithFullAccess() {

        review.setStatus(
                PerformanceReviewStatus.SELF_REVIEW
        );

        ManagerReviewRequest request =
                new ManagerReviewRequest();

        request.setReviewerScore(95.0);
        request.setFeedback("Approved by admin");

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                userPrincipal.getId()
        ).thenReturn(999L);

        // FIX Ở ĐÂY
        doReturn(
                List.of(
                        new SimpleGrantedAuthority(
                                "FULL_ACCESS"
                        )
                )
        ).when(userPrincipal)
                .getAuthorities();

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
                performanceReviewRepository.save(review)
        ).thenReturn(review);

        when(
                performanceReviewMapper.toResponse(review)
        ).thenReturn(reviewResponse);

        PerformanceReviewResponse result =
                performanceReviewService.managerReview(
                        userPrincipal,
                        REVIEW_ID,
                        request
                );

        assertThat(result)
                .isNotNull();

        assertThat(review.getReviewerScore())
                .isEqualTo(95.0);

        assertThat(review.getFeedback())
                .isEqualTo("Approved by admin");

        assertThat(review.getStatus())
                .isEqualTo(
                        PerformanceReviewStatus.MANAGER_REVIEW
                );
    }


    // =====================================================
    // COMPLETE REVIEW
    // =====================================================

    @Test
    @DisplayName(
            "Assigned reviewer completes performance review successfully"
    )
    void completeReview_success() {

        review.setStatus(
                PerformanceReviewStatus.MANAGER_REVIEW
        );


        Kpi achievedKpi =
                createKpi(
                        100.0,
                        100.0
                );

        Kpi failedKpi =
                createKpi(
                        70.0,
                        80.0
                );


        review.addKpi(
                achievedKpi
        );

        review.addKpi(
                failedKpi
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                REVIEWER_ID
        );

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
                performanceReviewRepository.save(
                        review
                )
        ).thenReturn(
                review
        );

        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PerformanceReviewResponse result =
                performanceReviewService.completeReview(
                        userPrincipal,
                        REVIEW_ID
                );


        assertThat(result)
                .isNotNull();


        verify(
                kpiCalculationService,
                times(1)
        ).calculateReviewKpis(
                review
        );


        assertThat(
                achievedKpi.getStatus()
        ).isEqualTo(
                KpiStatus.ACHIEVED
        );

        assertThat(
                failedKpi.getStatus()
        ).isEqualTo(
                KpiStatus.FAILED
        );

        assertThat(
                review.getStatus()
        ).isEqualTo(
                PerformanceReviewStatus.COMPLETED
        );
    }


    @Test
    @DisplayName(
            "Complete review skips deleted KPI when locking KPI statuses"
    )
    void completeReview_shouldIgnoreDeletedKpi() {

        review.setStatus(
                PerformanceReviewStatus.MANAGER_REVIEW
        );


        Kpi deletedKpi =
                createKpi(
                        100.0,
                        100.0
                );

        deletedKpi.setStatus(
                KpiStatus.IN_PROGRESS
        );

        ReflectionTestUtils.setField(
                deletedKpi,
                "deletedAt",
                Instant.now()
        );


        review.addKpi(
                deletedKpi
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                REVIEWER_ID
        );

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
                performanceReviewRepository.save(
                        review
                )
        ).thenReturn(
                review
        );

        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        performanceReviewService.completeReview(
                userPrincipal,
                REVIEW_ID
        );


        assertThat(
                deletedKpi.getStatus()
        ).isEqualTo(
                KpiStatus.IN_PROGRESS
        );
    }


    @Test
    @DisplayName(
            "Complete review throws exception when status is invalid"
    )
    void completeReview_throwsWhenStatusInvalid() {

        review.setStatus(
                PerformanceReviewStatus.SELF_REVIEW
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .completeReview(
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
                                        .PERFORMANCE_REVIEW_INVALID_STATUS
                        )
                );


        verifyNoInteractions(
                kpiCalculationService
        );
    }


    @Test
    @DisplayName(
            "Complete review throws exception when user is not assigned reviewer"
    )
    void completeReview_throwsWhenReviewerForbidden() {

        review.setStatus(
                PerformanceReviewStatus.MANAGER_REVIEW
        );


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                999L
        );

        when(
                userPrincipal.getAuthorities()
        ).thenReturn(
                Collections.emptyList()
        );

        when(
                performanceReviewRepository
                        .findByIdAndCompany_IdAndDeletedAtIsNull(
                                REVIEW_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(review)
        );


        assertThatThrownBy(
                () ->
                        performanceReviewService
                                .completeReview(
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
                                        .PERFORMANCE_REVIEW_REVIEWER_FORBIDDEN
                        )
                );


        verifyNoInteractions(
                kpiCalculationService
        );
    }


    @Test
    @DisplayName(
            "FULL_ACCESS user can complete review when not assigned reviewer"
    )
    void completeReview_successWithFullAccess() {

        review.setStatus(
                PerformanceReviewStatus.MANAGER_REVIEW
        );


        Kpi kpi =
                createKpi(
                        90.0,
                        80.0
                );

        review.addKpi(kpi);


        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                999L
        );

        doReturn(
                List.of(
                        new SimpleGrantedAuthority(
                                "FULL_ACCESS"
                        )
                )
        ).when(userPrincipal)
                .getAuthorities();

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
                performanceReviewRepository.save(
                        review
                )
        ).thenReturn(
                review
        );

        when(
                performanceReviewMapper.toResponse(
                        review
                )
        ).thenReturn(
                reviewResponse
        );


        PerformanceReviewResponse result =
                performanceReviewService.completeReview(
                        userPrincipal,
                        REVIEW_ID
                );


        assertThat(result)
                .isNotNull();

        assertThat(review.getStatus())
                .isEqualTo(
                        PerformanceReviewStatus.COMPLETED
                );

        assertThat(kpi.getStatus())
                .isEqualTo(
                        KpiStatus.ACHIEVED
                );
    }


    // =====================================================
    // SUMMARY
    // =====================================================

    @Test
    @DisplayName(
            "Get assigned review summary successfully"
    )
    void getMyAssignedReviewSummary_success() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                userPrincipal.getId()
        ).thenReturn(
                REVIEWER_ID
        );


        when(
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndDeletedAtIsNull(
                                COMPANY_ID,
                                REVIEWER_ID
                        )
        ).thenReturn(
                10L
        );


        when(
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                COMPANY_ID,
                                REVIEWER_ID,
                                PerformanceReviewStatus.DRAFT
                        )
        ).thenReturn(
                2L
        );


        when(
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                COMPANY_ID,
                                REVIEWER_ID,
                                PerformanceReviewStatus.SELF_REVIEW
                        )
        ).thenReturn(
                3L
        );


        when(
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                COMPANY_ID,
                                REVIEWER_ID,
                                PerformanceReviewStatus.MANAGER_REVIEW
                        )
        ).thenReturn(
                1L
        );


        when(
                performanceReviewRepository
                        .countByCompany_IdAndReviewer_IdAndStatusAndDeletedAtIsNull(
                                COMPANY_ID,
                                REVIEWER_ID,
                                PerformanceReviewStatus.COMPLETED
                        )
        ).thenReturn(
                4L
        );


        PerformanceReviewSummaryResponse result =
                performanceReviewService
                        .getMyAssignedReviewSummary(
                                userPrincipal
                        );


        assertThat(result)
                .isNotNull();

        assertThat(
                result.getTotalReviews()
        ).isEqualTo(
                10L
        );

        assertThat(
                result.getAwaitingSelfReview()
        ).isEqualTo(
                2L
        );

        assertThat(
                result.getAwaitingManagerReview()
        ).isEqualTo(
                3L
        );

        assertThat(
                result.getAwaitingCompletion()
        ).isEqualTo(
                1L
        );

        assertThat(
                result.getCompleted()
        ).isEqualTo(
                4L
        );
    }


    // =====================================================
    // HELPER METHODS
    // =====================================================

    private void mockValidCreateDependencies() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(
                COMPANY_ID
        );

        when(
                companyService.getCompanyById(
                        COMPANY_ID
                )
        ).thenReturn(
                company
        );

        when(
                employeeService
                        .getEmployeeEntityByIdAndCompanyId(
                                EMPLOYEE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                employee
        );

        when(
                userService
                        .getUserEntityByIdAndCompanyId(
                                REVIEWER_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                reviewer
        );

        when(
                userService.canBePerformanceReviewer(
                        REVIEWER_ID
                )
        ).thenReturn(
                true
        );

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );
    }


    private Kpi createKpi(
            double currentValue,
            double targetValue
    ) {

        return Kpi.builder()
                .company(company)
                .employee(employee)
                .title("Test KPI")
                .description("Test KPI")
                .metricType(
                        KpiMetricType.TASK_COMPLETION_RATE
                )
                .targetValue(
                        targetValue
                )
                .currentValue(
                        currentValue
                )
                .unit("%")
                .weight(100.0)
                .score(0.0)
                .startDate(PERIOD_START)
                .endDate(PERIOD_END)
                .status(
                        KpiStatus.IN_PROGRESS
                )
                .build();
    }
}