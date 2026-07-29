package com.lamdayne.humify.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryResponse {
    private KpiMetricsResponse kpis;
    private StatusOverviewResponse statusOverview;
    private List<PriorityCountResponse> priorityBreakdown;
    private List<TypeCountResponse> typesOfWork;
    private List<WorkloadResponse> teamWorkload;
    private List<RecentActivityResponse> recentActivities;
    private List<MemberPerformanceResponse> memberPerformance;
}
