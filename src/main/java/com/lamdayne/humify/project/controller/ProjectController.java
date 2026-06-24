package com.lamdayne.humify.project.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.PageResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.project.dto.request.UpdateMemberRoleRequest;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectMemberService projectMemberService;

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

}
