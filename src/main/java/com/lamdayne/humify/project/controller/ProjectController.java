    package com.lamdayne.humify.project.controller;

    import com.lamdayne.humify.auth.security.principal.UserPrincipal;
    import com.lamdayne.humify.common.response.ApiResponse;
    import com.lamdayne.humify.common.response.PageResponse;
    import com.lamdayne.humify.common.response.SuccessCode;
    import com.lamdayne.humify.project.dto.request.*;
    import com.lamdayne.humify.project.dto.response.InvitationResponse;
    import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
    import com.lamdayne.humify.project.dto.response.ProjectResponse;
    import com.lamdayne.humify.project.dto.response.SprintResponse;
    import com.lamdayne.humify.project.enums.SprintStatus;
    import com.lamdayne.humify.project.service.ProjectInvitationService;
    import com.lamdayne.humify.project.service.ProjectMemberService;
    import com.lamdayne.humify.project.service.ProjectService;
    import com.lamdayne.humify.project.service.SprintService;
    import jakarta.validation.Valid;
    import jakarta.validation.constraints.Min;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/projects")
    public class ProjectController {

        private final ProjectMemberService projectMemberService;
        private final ProjectService projectService;
        private final ProjectInvitationService projectInvitationService;
        private final SprintService sprintService;

        @PostMapping("/{projectId}/invitations")
        public ResponseEntity<ApiResponse<InvitationResponse>> createInvitation(
                @PathVariable Long projectId,
                @AuthenticationPrincipal UserPrincipal userPrincipal,
                @RequestBody @Valid CreateInvitationRequest request
        ) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            SuccessCode.INVITATION_CREATE_SUCCESS,
                            projectInvitationService.createInvitation(projectId, userPrincipal, request)
                    ));
        }

        @GetMapping("/{projectId}/members")
        public ResponseEntity<ApiResponse<PageResponse<ProjectMemberResponse>>> findAllMemberByProjectId(
                @PathVariable(name = "projectId") Long projectId,
                @RequestParam(defaultValue = "0", required = false) @Min(value = 0, message = "PAGE_NO_INVALID") int page,
                @RequestParam(defaultValue = "10", required = false) @Min(value = 10, message = "PAGE_SIZE_INVALID") int size,
                @RequestParam(required = false) String... sorts
        ) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.MEMBER_READ_SUCCESS,
                            projectMemberService.findAllByProjectId(projectId, page, size, sorts)
                    ));
        }

        @PutMapping("/{projectId}/members/{userId}/role")
        public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateMemberRole(
                @PathVariable(name = "projectId") Long projectId,
                @PathVariable(name = "userId") Long userId,
                @RequestBody @Valid UpdateMemberRoleRequest request
        ) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.MEMBER_UPDATE_ROLE_SUCCESS,
                            projectMemberService.updateMemberRole(projectId, userId, request)
                    ));
        }

        @DeleteMapping("/{projectId}/members/{userId}")
        public ResponseEntity<ApiResponse<Void>> deleteMember(
                @PathVariable(name = "projectId") Long projectId,
                @PathVariable(name = "userId") Long userId
        ) {
            projectMemberService.deleteMember(projectId, userId);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_REMOVE_SUCCESS));
        }

        @PostMapping("/{projectId}/members/{userId}/approve")
        public ResponseEntity<ApiResponse<ProjectMemberResponse>> approveMember(
                @PathVariable(name = "projectId") Long projectId,
                @PathVariable(name = "userId") Long userId
        ) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.MEMBER_APPROVE_SUCCESS,
                            projectMemberService.approveMember(projectId, userId)
                    ));
        }

        @PostMapping
        public ResponseEntity<ApiResponse<ProjectResponse>>  createProject(
                @RequestBody @Valid CreateProjectRequest request
        ){
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.PROJECT_CREATE_SUCCESS,
                            projectService.createProject(request)
                    ));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> getAllProjects(
                @RequestParam(defaultValue = "0")
                @Min(value = 0, message = "PAGE_NO_INVALID")
                int page,
                @RequestParam(defaultValue = "10")
                @Min(value = 10, message = "PAGE_SIZE_INVALID")
                int size,
                @RequestParam(required = false)
                String... sorts
        ){
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.PROJECT_READ_SUCCESS,
                            projectService.getAllProject(page, size, sorts)
                    ));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
                @PathVariable long id
        ){
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.PROJECT_READ_SUCCESS,
                            projectService.getById(id)
                    ));

        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
                @PathVariable long id, @RequestBody @Valid UpdateProjectRequest request
        ){
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.PROJECT_UPDATE_SUCCESS,
                            projectService.updateProject(id, request)
                    ));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteProject(
                @PathVariable long id
        ){
            projectService.deleteProject(id);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(SuccessCode.PROJECT_DELETE_SUCCESS));
        }

        @PostMapping("/{projectId}/sprints")
        public ResponseEntity<ApiResponse<SprintResponse>> createSprint(
                @PathVariable Long projectId,
                @Valid @RequestBody CreateSprintRequest request) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.SPRINT_CREATE_SUCCESS,
                            sprintService.createSprint(projectId, request)
                    ));
        }

        @GetMapping("/{projectId}/sprints")
        public ResponseEntity<ApiResponse<List<SprintResponse>>> getSprints(
                @PathVariable Long projectId,
                @RequestParam(required = false) SprintStatus status) {
            return ResponseEntity.ok()
                    .body(ApiResponse.success(
                            SuccessCode.SPRINT_READ_SUCCESS,
                            sprintService.getSprints(projectId, status)
                    ));
        }

    }
