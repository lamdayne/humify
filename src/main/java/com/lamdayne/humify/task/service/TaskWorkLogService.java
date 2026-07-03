package com.lamdayne.humify.task.service;

import com.lamdayne.humify.task.dto.request.CreateWorklogRequest;
import com.lamdayne.humify.task.dto.request.UpdateWorklogRequest;
import com.lamdayne.humify.task.dto.response.WorklogResponse;

import java.util.List;

public interface TaskWorkLogService {
    WorklogResponse create(Long taskId, CreateWorklogRequest request);

    List<WorklogResponse> getByTask(Long taskId);

    WorklogResponse update(Long id, UpdateWorklogRequest request);

    void delete(Long id);
}
