package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.employee.repository.EmployeeRepository;
import com.lamdayne.humify.performance.dto.request.ReviewRequests;
import com.lamdayne.humify.performance.dto.response.ReviewResponse;
import com.lamdayne.humify.performance.dto.response.SystemCalculatedMetricsResponse;
import com.lamdayne.humify.performance.entity.PerformanceReview;
import com.lamdayne.humify.performance.enums.PerformanceReviewStatus;
import com.lamdayne.humify.performance.mapper.PerformanceReviewMapper;
import com.lamdayne.humify.performance.repository.PerformanceReviewRepository;
import com.lamdayne.humify.performance.service.impl.PerformanceReviewServiceImpl;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.enums.TaskType;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceReviewServiceImpl Tests")
class PerformanceReviewServiceImplTest {

    @Mock private PerformanceReviewRepository reviewRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private UserRepository userRepository;
    @Mock private PerformanceReviewMapper reviewMapper;
    @Mock private TaskRepository taskRepository;

    @InjectMocks
    private PerformanceReviewServiceImpl performanceReviewService;

    private Employee employee;
    private User reviewer;
    private User employeeUser;
    private PerformanceReview review;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", 1L);
        employee.setEmail("nv@company.com");

        reviewer = new User();
        ReflectionTestUtils.setField(reviewer, "id", 10L);

        employeeUser = new User();
        ReflectionTestUtils.setField(employeeUser, "id", 20L);
        employeeUser.setEmail("nv@company.com");

        review = PerformanceReview.builder()
                .employee(employee)
                .reviewer(reviewer)
                .reviewPeriod("Q3-2026")
                .status(PerformanceReviewStatus.DRAFT)
                .build();

        reviewResponse = ReviewResponse.builder()
                .id(1L)
                .employeeId(1L)
                .reviewerId(10L)
                .reviewPeriod("Q3-2026")
                .status(PerformanceReviewStatus.DRAFT)
                .systemCalculatedMetrics(
                    SystemCalculatedMetricsResponse.builder()
                        .taskCompletionRate(0.0).bugLeakageRate(0.0)
                        .onTimeDeliveryRate(0.0).worklogBurnedRate(0.0)
                        .build()
                )
                .build();
    }

    // ---- createReview ----

    @Test
    @DisplayName("Create performance review successfully when employee has no tasks")
    void createReview_success_noTasks() {
        ReviewRequests.CreateReviewRequest request = mock(ReviewRequests.CreateReviewRequest.class);
        when(request.getReviewerId()).thenReturn(10L);
        when(request.getReviewPeriod()).thenReturn("Q3-2026");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByEmployeeIdAndReviewPeriod(1L, "Q3-2026")).thenReturn(false);
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.of(employeeUser));
        when(taskRepository.findByAssignee_Id(20L)).thenReturn(Collections.emptyList());
        when(reviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        ReviewResponse result = performanceReviewService.createReview(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getReviewPeriod()).isEqualTo("Q3-2026");
    }

    @Test
    @DisplayName("Create performance review successfully when employee has tasks")
    void createReview_success_withVariousTasks() {
        ReviewRequests.CreateReviewRequest request = mock(ReviewRequests.CreateReviewRequest.class);
        when(request.getReviewerId()).thenReturn(10L);
        when(request.getReviewPeriod()).thenReturn("Q3-2026");

        Task completedTask = new Task();
        completedTask.setType(TaskType.STORY);
        completedTask.setCompletedAt(Instant.now());

        Task pendingTask = new Task();
        pendingTask.setType(TaskType.STORY);
        pendingTask.setCompletedAt(null);

        Task bugTask = new Task();
        bugTask.setType(TaskType.BUG);
        bugTask.setCompletedAt(null);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByEmployeeIdAndReviewPeriod(1L, "Q3-2026")).thenReturn(false);
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.of(employeeUser));
        when(taskRepository.findByAssignee_Id(20L)).thenReturn(List.of(completedTask, pendingTask, bugTask));
        when(reviewRepository.save(any())).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        ReviewResponse result = performanceReviewService.createReview(1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Create performance review throws exception when employee not found")
    void createReview_throwsWhenEmployeeNotFound() {
        ReviewRequests.CreateReviewRequest request = mock(ReviewRequests.CreateReviewRequest.class);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.createReview(99L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Test
    @DisplayName("Create performance review throws exception when reviewer not found")
    void createReview_throwsWhenReviewerNotFound() {
        ReviewRequests.CreateReviewRequest request = mock(ReviewRequests.CreateReviewRequest.class);
        when(request.getReviewerId()).thenReturn(999L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.createReview(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("Create performance review throws exception when period is duplicate")
    void createReview_throwsWhenPeriodDuplicate() {
        ReviewRequests.CreateReviewRequest request = mock(ReviewRequests.CreateReviewRequest.class);
        when(request.getReviewerId()).thenReturn(10L);
        when(request.getReviewPeriod()).thenReturn("Q3-2026");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByEmployeeIdAndReviewPeriod(1L, "Q3-2026")).thenReturn(true);

        assertThatThrownBy(() -> performanceReviewService.createReview(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_PERIOD_DUPLICATE));
    }

    @Test
    @DisplayName("Create performance review throws exception when employee user account not found")
    void createReview_throwsWhenEmployeeUserNotFound() {
        ReviewRequests.CreateReviewRequest request = mock(ReviewRequests.CreateReviewRequest.class);
        when(request.getReviewerId()).thenReturn(10L);
        when(request.getReviewPeriod()).thenReturn("Q3-2026");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(10L)).thenReturn(Optional.of(reviewer));
        when(reviewRepository.existsByEmployeeIdAndReviewPeriod(1L, "Q3-2026")).thenReturn(false);
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.createReview(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // ---- getReviewsByEmployeeId ----

    @Test
    @DisplayName("Get reviews by employee ID returns list")
    void getReviewsByEmployeeId_success() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByEmployeeIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        List<ReviewResponse> result = performanceReviewService.getReviewsByEmployeeId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Get reviews by employee ID returns empty list")
    void getReviewsByEmployeeId_empty() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByEmployeeIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        List<ReviewResponse> result = performanceReviewService.getReviewsByEmployeeId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Get reviews by employee ID throws exception when employee not found")
    void getReviewsByEmployeeId_throwsWhenEmployeeNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> performanceReviewService.getReviewsByEmployeeId(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    // ---- getReviewById ----

    @Test
    @DisplayName("Get review by ID returns valid review")
    void getReviewById_success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        ReviewResponse result = performanceReviewService.getReviewById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Get review by ID throws exception when not found")
    void getReviewById_throwsWhenNotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.getReviewById(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }

    // ---- submitSelfScore ----

    @Test
    @DisplayName("Submit self score successfully")
    void submitSelfScore_success() {
        ReviewRequests.SubmitSelfScoreRequest request = mock(ReviewRequests.SubmitSelfScoreRequest.class);
        when(request.getSelfScore()).thenReturn(4.0);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.of(employeeUser));
        when(reviewRepository.save(any())).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        ReviewResponse result = performanceReviewService.submitSelfScore(1L, request, 20L);

        assertThat(result).isNotNull();
        assertThat(review.getStatus()).isEqualTo(PerformanceReviewStatus.SELF_REVIEW);
    }

    @Test
    @DisplayName("Submit self score throws exception when review not found")
    void submitSelfScore_throwsWhenNotFound() {
        ReviewRequests.SubmitSelfScoreRequest request = mock(ReviewRequests.SubmitSelfScoreRequest.class);
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.submitSelfScore(99L, request, 20L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("Submit self score throws exception when employee user not found")
    void submitSelfScore_throwsWhenUserNotFound() {
        ReviewRequests.SubmitSelfScoreRequest request = mock(ReviewRequests.SubmitSelfScoreRequest.class);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.submitSelfScore(1L, request, 20L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    @DisplayName("Submit self score throws exception when user is not the employee")
    void submitSelfScore_throwsWhenWrongUser() {
        ReviewRequests.SubmitSelfScoreRequest request = mock(ReviewRequests.SubmitSelfScoreRequest.class);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.of(employeeUser));

        assertThatThrownBy(() -> performanceReviewService.submitSelfScore(1L, request, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("Submit self score throws exception when review status is not DRAFT")
    void submitSelfScore_throwsWhenNotDraft() {
        ReviewRequests.SubmitSelfScoreRequest request = mock(ReviewRequests.SubmitSelfScoreRequest.class);
        review.setStatus(PerformanceReviewStatus.SELF_REVIEW);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("nv@company.com")).thenReturn(Optional.of(employeeUser));

        assertThatThrownBy(() -> performanceReviewService.submitSelfScore(1L, request, 20L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_STATUS_INVALID));
    }

    // ---- submitReviewerScore ----

    @Test
    @DisplayName("Submit reviewer score successfully")
    void submitReviewerScore_success() {
        ReviewRequests.SubmitReviewerScoreRequest request = mock(ReviewRequests.SubmitReviewerScoreRequest.class);
        when(request.getReviewerScore()).thenReturn(4.5);
        when(request.getFeedback()).thenReturn("Good job!");

        review.setStatus(PerformanceReviewStatus.SELF_REVIEW);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        ReviewResponse result = performanceReviewService.submitReviewerScore(1L, request, 10L);

        assertThat(result).isNotNull();
        assertThat(review.getStatus()).isEqualTo(PerformanceReviewStatus.MANAGER_REVIEW);
    }

    @Test
    @DisplayName("Submit reviewer score throws exception when review not found")
    void submitReviewerScore_throwsWhenNotFound() {
        ReviewRequests.SubmitReviewerScoreRequest request = mock(ReviewRequests.SubmitReviewerScoreRequest.class);
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.submitReviewerScore(99L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("Submit reviewer score throws exception when user is not the reviewer")
    void submitReviewerScore_throwsWhenWrongReviewer() {
        ReviewRequests.SubmitReviewerScoreRequest request = mock(ReviewRequests.SubmitReviewerScoreRequest.class);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> performanceReviewService.submitReviewerScore(1L, request, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("Submit reviewer score throws exception when review status is COMPLETED")
    void submitReviewerScore_throwsWhenCompleted() {
        ReviewRequests.SubmitReviewerScoreRequest request = mock(ReviewRequests.SubmitReviewerScoreRequest.class);
        review.setStatus(PerformanceReviewStatus.COMPLETED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> performanceReviewService.submitReviewerScore(1L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_STATUS_INVALID));
    }

    // ---- completeReview ----

    @Test
    @DisplayName("Complete review successfully")
    void completeReview_success() {
        ReviewRequests.CompleteReviewRequest request = mock(ReviewRequests.CompleteReviewRequest.class);
        when(request.getFinalScore()).thenReturn(4.7);

        review.setStatus(PerformanceReviewStatus.MANAGER_REVIEW);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        ReviewResponse result = performanceReviewService.completeReview(1L, request, 10L);

        assertThat(result).isNotNull();
        assertThat(review.getStatus()).isEqualTo(PerformanceReviewStatus.COMPLETED);
    }

    @Test
    @DisplayName("Complete review throws exception when review not found")
    void completeReview_throwsWhenNotFound() {
        ReviewRequests.CompleteReviewRequest request = mock(ReviewRequests.CompleteReviewRequest.class);
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceReviewService.completeReview(99L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("Complete review throws exception when user is not the reviewer")
    void completeReview_throwsWhenWrongReviewer() {
        ReviewRequests.CompleteReviewRequest request = mock(ReviewRequests.CompleteReviewRequest.class);
        review.setStatus(PerformanceReviewStatus.MANAGER_REVIEW);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> performanceReviewService.completeReview(1L, request, 999L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("Complete review throws exception when review status is not MANAGER_REVIEW")
    void completeReview_throwsWhenWrongStatus() {
        ReviewRequests.CompleteReviewRequest request = mock(ReviewRequests.CompleteReviewRequest.class);
        review.setStatus(PerformanceReviewStatus.SELF_REVIEW);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> performanceReviewService.completeReview(1L, request, 10L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_STATUS_INVALID));
    }
}