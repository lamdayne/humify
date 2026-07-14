package com.lamdayne.humify.project.service.impl;

import com.lamdayne.humify.auth.security.rls.CompanyContext;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.util.PageableUtil;
import com.lamdayne.humify.company.entity.Company;
import com.lamdayne.humify.company.repository.CompanyRepository;
import com.lamdayne.humify.project.dto.request.CreateProjectRequest;
import com.lamdayne.humify.project.dto.request.UpdateProjectRequest;
import com.lamdayne.humify.project.dto.response.ProjectResponse;
import com.lamdayne.humify.project.entity.Project;
import com.lamdayne.humify.project.entity.ProjectMember;
import com.lamdayne.humify.project.entity.ProjectRole;
import com.lamdayne.humify.project.enums.ProjectMemberStatus;
import com.lamdayne.humify.project.enums.ProjectRoleCode;
import com.lamdayne.humify.project.enums.ProjectStatus;
import com.lamdayne.humify.project.mapper.ProjectMapper;
import com.lamdayne.humify.project.repository.ProjectMemberRepository;
import com.lamdayne.humify.project.repository.ProjectRepository;
import com.lamdayne.humify.project.repository.ProjectRoleRepository;
import com.lamdayne.humify.project.service.BoardColumnService;
import com.lamdayne.humify.project.service.ProjectService;
import com.lamdayne.humify.user.entity.User;
import com.lamdayne.humify.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final BoardColumnService boardColumnService;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        long companyId = CompanyContext.getCompanyId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        if (projectRepository.existsByCompanyIdAndKey(companyId, request.getKey())) {
            throw new AppException(ErrorCode.PROJECT_KEY_EXISTED);
        }

        Project project = projectMapper.toEntity(request);
        project.setCompany(company);
        project.setCreator(creator);
        project.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.save(project);

        boardColumnService.initDefaultColumns(project);

        ProjectRole projectRole = projectRoleRepository.findByName(ProjectRoleCode.MANAGER.name())
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_ROLE_NOT_FOUND));

        ProjectMember projectMember = ProjectMember.builder()
                .project(project)
                .projectRole(projectRole)
                .status(ProjectMemberStatus.ACTIVE)
                .user(creator)
                .build();

        projectMemberRepository.save(projectMember);

        return projectMapper.toResponse(project);
    }

    @Override
    public PageResponse<ProjectResponse> getAllProject(int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);


        Page<Project> projects = projectRepository.findAll(pageable);
        List<ProjectResponse> projectResponses = projects.stream()
                .map(projectMapper::toResponse)
                .toList();
        return PageResponse.<ProjectResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(projects.getTotalPages())
                .totalElements(projects.getTotalElements())
                .items(projectResponses)
                .build();
    }

    @Override
    public ProjectResponse getById(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        projectMapper.updateEntity(request, project);
        projectRepository.save(project);

        return projectMapper.toResponse(project);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
        projectRepository.delete(project);
    }

    @Override
    public PageResponse<ProjectResponse> getProjectsByCompany(Long companyId, int page, int size, String... sorts) {
        Pageable pageable = PageableUtil.buildPageable(page, size, sorts);
        Page<Project> projects = projectRepository.findByCompanyId(companyId, pageable);
        List<ProjectResponse> items = projects.stream().map(projectMapper::toResponse).toList();

        return PageResponse.<ProjectResponse>builder()
                .pageNo(page)
                .pageSize(size)
                .totalPages(projects.getTotalPages())
                .totalElements(projects.getTotalElements())
                .items(items)
                .build();
    }

    @Override
    public Project findById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    public Long incrementAndGetIssueCounter(Long projectId) {
        projectRepository.incrementIssueCounter(projectId);
        return projectRepository.getIssueCounter(projectId);
    }


}
