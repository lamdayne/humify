package com.lamdayne.humify.project.service;

import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.project.dto.request.UpdateMemberRoleRequest;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;

public interface ProjectMemberService {

    PageResponse<ProjectMemberResponse> findAllByProjectId(Long projectId, int page, int size, String... sorts);

    ProjectMemberResponse updateMemberRole(Long projectId, Long userId, UpdateMemberRoleRequest request);

    void deleteMember(Long projectId, Long userId);

    ProjectMemberResponse approveMember(Long projectId, Long userId);

}
