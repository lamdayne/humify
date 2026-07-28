package com.lamdayne.humify.project.service;

import com.lamdayne.humify.project.dto.response.ProjectSummaryResponse;

public interface ProjectSummaryService {
    ProjectSummaryResponse getProjectSummary(Long projectId);
}
