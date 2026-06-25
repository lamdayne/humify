package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.project.dto.request.UpdateMemberRoleRequest;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.entity.ProjectMember;
import com.lamdayne.humify.project.entity.ProjectRole;
import com.lamdayne.humify.project.enums.ProjectMemberStatus;
import com.lamdayne.humify.project.mapper.ProjectMemberMapper;
import com.lamdayne.humify.project.repository.ProjectMemberRepository;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.repository.ProjectRoleRepository;
import com.lamdayne.humify.project.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public PageResponse<ProjectMemberResponse> findAllByProjectId(Long projectId, int page, int size, String... sorts) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<ProjectMember> projectMemberPage = projectMemberRepository.findAllByProjectId(projectId, pageable);

        List<ProjectMemberResponse> projectMemberResponses = projectMemberPage.stream()
                .map(projectMemberMapper::toProjectMemberResponse)
                .toList();

        return PageResponse.<ProjectMemberResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalElements(projectMemberPage.getTotalElements())
                .totalPages(projectMemberPage.getTotalPages())
                .items(projectMemberResponses)
                .build();
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateMemberRole(Long projectId, Long userId, UpdateMemberRoleRequest request) {
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        ProjectRole projectRole = projectRoleRepository.findByName(request.getCode())
                        .orElseThrow(() -> new AppException(ErrorCode.PROJECT_ROLE_NOT_FOUND));

        projectMember.setProjectRole(projectRole);

        return projectMemberMapper.toProjectMemberResponse(projectMemberRepository.save(projectMember));
    }

    @Override
    public void deleteMember(Long projectId, Long userId) {
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        projectMember.setStatus(ProjectMemberStatus.INACTIVE);
        projectMemberRepository.save(projectMember);
    }

    @Override
    public ProjectMemberResponse approveMember(Long projectId, Long userId) {
        ProjectMember projectMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        projectMember.setStatus(ProjectMemberStatus.ACTIVE);

        return projectMemberMapper.toProjectMemberResponse(projectMemberRepository.save(projectMember));
    }
}
