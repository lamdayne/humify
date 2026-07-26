package com.lamdayne.humify.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPerformanceResponse {
    private Long userId;
    private String userName;
    private String email;
    private String avatar;
    private long totalTasks;
    private long completedTasks;
    private long onTimeCompletedTasks;
    private double estimatedHoursSum;
    private double loggedHoursSum;
    private double completionRate;
    private double timeEfficiency;
    private double onTimeRate;
    private double overallScore;
}
