package com.lamdayne.humify.project.service;

import com.lamdayne.humify.project.dto.request.CreateSprintRequest;
import com.lamdayne.humify.project.dto.request.UpdateSprintRequest;
import com.lamdayne.humify.project.dto.request.UpdateSprintStatusRequest;
import com.lamdayne.humify.project.dto.response.SprintResponse;
import com.lamdayne.humify.project.enums.SprintStatus;

import java.util.List;

public interface SprintService {
    SprintResponse createSprint(Long projectId, CreateSprintRequest request);
    List<SprintResponse> getSprints(Long projectId, SprintStatus status);
    SprintResponse updateSprint(Long id, UpdateSprintRequest request);
    SprintResponse updateSprintStatus(Long id, UpdateSprintStatusRequest request);
    void deleteSprint(Long id);
}
