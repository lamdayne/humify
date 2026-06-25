package com.lamdayne.humify.project.controller;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.project.dto.request.AcceptInvitationRequest;
import com.lamdayne.humify.project.dto.response.ProjectMemberResponse;
import com.lamdayne.humify.project.dto.response.ValidateInvitationResponse;
import com.lamdayne.humify.project.service.ProjectInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/invitations")
public class ProjectInvitationController {

    private final ProjectInvitationService projectInvitationService;

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateInvitationResponse>> validateInvitation(
            @RequestParam String token
    ) {
        ValidateInvitationResponse response = projectInvitationService.validateInvitation(token);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.INVITATION_VALIDATE_SUCCESS, response));
    }

    @PostMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> acceptInvitation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody @Valid AcceptInvitationRequest request
    ) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.INVITATION_ACCEPT_SUCCESS,
                        projectInvitationService.acceptInvitation(userPrincipal, request)
                ));
    }
}