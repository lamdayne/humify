package com.lamdayne.humify.project.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.project.dto.request.CreateProjectRequest;
import com.lamdayne.humify.project.dto.request.UpdateProjectRequest;
import com.lamdayne.humify.project.dto.response.ProjectResponse;
import com.lamdayne.humify.project.entity.Project;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    PageResponse<ProjectResponse> getAllProject(int page, int size, String... sorts);

    ProjectResponse getById(Long id);

    ProjectResponse updateProject(Long id, UpdateProjectRequest request);

    void deleteProject(Long id);

    PageResponse<ProjectResponse> getProjectsByCompany(Long companyId, int page, int size, String... sorts);

    Project findById(Long id);

}
