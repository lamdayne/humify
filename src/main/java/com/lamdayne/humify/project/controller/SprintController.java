package com.lamdayne.humify.project.controller;

import com.lamdayne.humify.common.response.ApiResponse;
import com.lamdayne.humify.common.response.SuccessCode;
import com.lamdayne.humify.project.dto.request.CreateSprintRequest;
import com.lamdayne.humify.project.dto.request.UpdateSprintRequest;
import com.lamdayne.humify.project.dto.request.UpdateSprintStatusRequest;
import com.lamdayne.humify.project.dto.response.SprintResponse;
import com.lamdayne.humify.project.enums.SprintStatus;
import com.lamdayne.humify.project.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;


    @PutMapping("/sprints/{id}")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSprintRequest request) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.SPRINT_UPDATE_SUCCESS,
                        sprintService.updateSprint(id, request)
                ));
    }

    @PutMapping("/sprints/{id}/status")
    public ResponseEntity<ApiResponse<SprintResponse>> updateSprintStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSprintStatusRequest request) {
        return ResponseEntity.ok()
                .body(ApiResponse.success(
                        SuccessCode.SPRINT_STATUS_UPDATE_SUCCESS,
                        sprintService.updateSprintStatus(id, request)
                ));
    }

    @DeleteMapping("/sprints/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSprint(@PathVariable Long id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.SPRINT_DELETE_SUCCESS));
    }
}
