package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.employee.entity.Employee;
import com.lamdayne.humify.project.dto.response.*;
import com.lamdayne.humify.project.entity.BoardColumn;
import com.lamdayne.humify.project.enums.ColumnCategory;
import com.lamdayne.humify.project.repository.BoardColumnRepository;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.service.ProjectSummaryService;
import com.lamdayne.humify.task.entity.Task;
import com.lamdayne.humify.task.entity.TaskActivity;
import com.lamdayne.humify.task.enums.TaskPriority;
import com.lamdayne.humify.task.enums.TaskType;
import com.lamdayne.humify.task.repository.TaskActivityRepository;
import com.lamdayne.humify.task.repository.TaskRepository;
import com.lamdayne.humify.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectSummaryServiceImpl implements ProjectSummaryService {

    private final ProjectRepository projectRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final TaskActivityRepository taskActivityRepository;

    @Override
    @Transactional(readOnly = true)
    public ProjectSummaryResponse getProjectSummary(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        List<BoardColumn> columns = boardColumnRepository.findAllByProjectIdOrderByPositionAsc(projectId);
        List<Task> tasks = taskRepository.findByProjectId(projectId);

        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant sevenDaysFromNow = now.plus(7, ChronoUnit.DAYS);

        return ProjectSummaryResponse.builder()
                .kpis(computeKpis(tasks, now, sevenDaysAgo, sevenDaysFromNow))
                .statusOverview(computeStatusOverview(columns, tasks))
                .priorityBreakdown(computePriorityBreakdown(tasks))
                .typesOfWork(computeTypesOfWork(tasks))
                .teamWorkload(computeTeamWorkload(tasks))
                .recentActivities(fetchRecentActivities(projectId))
                .memberPerformance(computeMemberPerformance(tasks))
                .build();
    }

    private KpiMetricsResponse computeKpis(List<Task> tasks, Instant now, Instant sevenDaysAgo, Instant sevenDaysFromNow) {
        long completedLast7Days = tasks.stream()
                .filter(t -> (t.getColumn() != null && t.getColumn().getCategory() == ColumnCategory.DONE) || t.getCompletedAt() != null)
                .filter(t -> (t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(sevenDaysAgo)) ||
                        (t.getCompletedAt() != null && t.getCompletedAt().isAfter(sevenDaysAgo)))
                .count();

        long updatedLast7Days = tasks.stream()
                .filter(t -> t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(sevenDaysAgo))
                .count();

        long createdLast7Days = tasks.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(sevenDaysAgo))
                .count();

        long dueSoonNext7Days = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(now) && t.getDueDate().isBefore(sevenDaysFromNow))
                .filter(t -> t.getColumn() == null || t.getColumn().getCategory() != ColumnCategory.DONE)
                .count();

        return KpiMetricsResponse.builder()
                .completedLast7Days(completedLast7Days)
                .updatedLast7Days(updatedLast7Days)
                .createdLast7Days(createdLast7Days)
                .dueSoonNext7Days(dueSoonNext7Days)
                .build();
    }

    private StatusOverviewResponse computeStatusOverview(List<BoardColumn> columns, List<Task> tasks) {
        long totalWorkItems = tasks.size();
        List<StatusCountResponse> statusCounts = new ArrayList<>();

        for (BoardColumn col : columns) {
            long count = tasks.stream()
                    .filter(t -> t.getColumn() != null && Objects.equals(t.getColumn().getId(), col.getId()))
                    .count();
            statusCounts.add(StatusCountResponse.builder()
                    .columnId(col.getId())
                    .name(col.getName())
                    .category(col.getCategory() != null ? col.getCategory().name() : "TO_DO")
                    .count(count)
                    .build());
        }

        return StatusOverviewResponse.builder()
                .totalWorkItems(totalWorkItems)
                .statusCounts(statusCounts)
                .build();
    }

    private List<PriorityCountResponse> computePriorityBreakdown(List<Task> tasks) {
        Map<TaskPriority, Long> priorityCounts = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority() != null ? t.getPriority() : TaskPriority.MEDIUM,
                        Collectors.counting()
                ));

        List<PriorityCountResponse> priorityBreakdown = new ArrayList<>();
        for (TaskPriority p : TaskPriority.values()) {
            priorityBreakdown.add(PriorityCountResponse.builder()
                    .priority(p.name())
                    .count(priorityCounts.getOrDefault(p, 0L))
                    .build());
        }
        return priorityBreakdown;
    }

    private List<TypeCountResponse> computeTypesOfWork(List<Task> tasks) {
        long totalWorkItems = tasks.size();
        Map<TaskType, Long> typeCounts = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getType() != null ? t.getType() : TaskType.TASK,
                        Collectors.counting()
                ));

        List<TypeCountResponse> typesOfWork = new ArrayList<>();
        for (TaskType type : TaskType.values()) {
            long count = typeCounts.getOrDefault(type, 0L);
            double percentage = totalWorkItems > 0 ? Math.round((count * 100.0 / totalWorkItems) * 10.0) / 10.0 : 0.0;
            typesOfWork.add(TypeCountResponse.builder()
                    .type(type.name())
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        return typesOfWork;
    }

    private List<WorkloadResponse> computeTeamWorkload(List<Task> tasks) {
        long totalWorkItems = tasks.size();
        Map<User, Long> userWorkloadMap = tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(Task::getAssignee, Collectors.counting()));

        long unassignedCount = tasks.stream().filter(t -> t.getAssignee() == null).count();
        List<WorkloadResponse> teamWorkload = new ArrayList<>();

        if (unassignedCount > 0 || userWorkloadMap.isEmpty()) {
            double unassignedPercentage = totalWorkItems > 0 ? Math.round((unassignedCount * 100.0 / totalWorkItems) * 10.0) / 10.0 : 0.0;
            teamWorkload.add(WorkloadResponse.builder()
                    .userId(null)
                    .userName("Unassigned")
                    .email(null)
                    .avatar(null)
                    .count(unassignedCount)
                    .percentage(unassignedPercentage)
                    .build());
        }

        for (Map.Entry<User, Long> entry : userWorkloadMap.entrySet()) {
            User user = entry.getKey();
            long count = entry.getValue();
            double percentage = totalWorkItems > 0 ? Math.round((count * 100.0 / totalWorkItems) * 10.0) / 10.0 : 0.0;

            teamWorkload.add(WorkloadResponse.builder()
                    .userId(user.getId())
                    .userName(getUserDisplayName(user))
                    .email(user.getEmail())
                    .avatar(getUserAvatar(user))
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        return teamWorkload;
    }

    private List<RecentActivityResponse> fetchRecentActivities(Long projectId) {
        List<TaskActivity> activities = taskActivityRepository.findByTaskProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(0, 15));
        return activities.stream().map(a -> {
            User user = a.getUser();
            Task task = a.getTask();

            return RecentActivityResponse.builder()
                    .id(a.getId())
                    .taskId(task != null ? task.getId() : null)
                    .taskKey(task != null ? task.getTaskKey() : null)
                    .taskTitle(task != null ? task.getTitle() : null)
                    .taskColumnName(task != null && task.getColumn() != null ? task.getColumn().getName() : null)
                    .userName(getUserDisplayName(user))
                    .userAvatar(getUserAvatar(user))
                    .action(a.getAction() != null ? a.getAction().name() : "UPDATED")
                    .oldValue(a.getOldValue())
                    .newValue(a.getNewValue())
                    .createdAt(a.getCreatedAt())
                    .build();
        }).toList();
    }

    private List<MemberPerformanceResponse> computeMemberPerformance(List<Task> tasks) {
        // Exclude subtasks (only top-level tasks where parent == null)
        // Group strictly by Assignee (Assignee != null)
        Map<User, List<Task>> userTasksMap = tasks.stream()
                .filter(t -> t.getParent() == null)
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(Task::getAssignee));

        List<MemberPerformanceResponse> memberPerformanceList = new ArrayList<>();

        for (Map.Entry<User, List<Task>> entry : userTasksMap.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();

            long totalTasks = userTasks.size();
            long completedTasks = userTasks.stream()
                    .filter(t -> (t.getColumn() != null && t.getColumn().getCategory() == ColumnCategory.DONE) || t.getCompletedAt() != null)
                    .count();

            long onTimeCompletedTasks = userTasks.stream()
                    .filter(t -> (t.getColumn() != null && t.getColumn().getCategory() == ColumnCategory.DONE) || t.getCompletedAt() != null)
                    .filter(t -> t.getDueDate() == null ||
                            (t.getCompletedAt() != null && !t.getCompletedAt().isAfter(t.getDueDate())) ||
                            (t.getUpdatedAt() != null && !t.getUpdatedAt().isAfter(t.getDueDate())))
                    .count();

            double estimatedHoursSum = userTasks.stream()
                    .mapToDouble(t -> t.getEstimatedHours() != null ? t.getEstimatedHours() : 0.0)
                    .sum();

            double loggedHoursSum = userTasks.stream()
                    .mapToDouble(t -> t.getLoggedHours() != null ? t.getLoggedHours() : 0.0)
                    .sum();

            double completionRate = totalTasks > 0 ? Math.round((completedTasks * 100.0 / totalTasks) * 10.0) / 10.0 : 0.0;

            double timeEfficiency;
            if (loggedHoursSum > 0) {
                timeEfficiency = Math.round((estimatedHoursSum * 100.0 / loggedHoursSum) * 10.0) / 10.0;
            } else if (completedTasks > 0) {
                timeEfficiency = 100.0;
            } else {
                timeEfficiency = 0.0;
            }

            double onTimeRate = completedTasks > 0 ? Math.round((onTimeCompletedTasks * 100.0 / completedTasks) * 10.0) / 10.0 : 100.0;

            double boundedTimeEff = Math.min(100.0, timeEfficiency);
            double overallScore = Math.round(((0.4 * completionRate) + (0.3 * boundedTimeEff) + (0.3 * onTimeRate)) * 10.0) / 10.0;

            memberPerformanceList.add(MemberPerformanceResponse.builder()
                    .userId(user.getId())
                    .userName(getUserDisplayName(user))
                    .email(user.getEmail())
                    .avatar(getUserAvatar(user))
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .onTimeCompletedTasks(onTimeCompletedTasks)
                    .estimatedHoursSum(Math.round(estimatedHoursSum * 10.0) / 10.0)
                    .loggedHoursSum(Math.round(loggedHoursSum * 10.0) / 10.0)
                    .completionRate(completionRate)
                    .timeEfficiency(timeEfficiency)
                    .onTimeRate(onTimeRate)
                    .overallScore(overallScore)
                    .build());
        }

        memberPerformanceList.sort(Comparator.comparingDouble(MemberPerformanceResponse::getOverallScore).reversed());
        return memberPerformanceList;
    }

    private String getUserDisplayName(User user) {
        if (user == null) return "System";
        if (user.getEmployee() != null) {
            Employee emp = user.getEmployee();
            if (emp.getFullName() != null && !emp.getFullName().isBlank()) {
                return emp.getFullName();
            }
        }
        return user.getEmail() != null ? user.getEmail() : "Unknown User";
    }

    private String getUserAvatar(User user) {
        if (user != null && user.getEmployee() != null) {
            return user.getEmployee().getAvatarUrl();
        }
        return null;
    }
}
