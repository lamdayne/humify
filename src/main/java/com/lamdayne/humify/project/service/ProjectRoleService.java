package com.lamdayne.humify.project.service;

import com.lamdayne.humify.project.dto.response.ProjectRoleResponse;

import java.util.List;

public interface ProjectRoleService {

    void initProjectRole();

    List<ProjectRoleResponse> getAllProjectRole();

}
