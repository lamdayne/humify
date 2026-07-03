package com.lamdayne.humify.task.service;

import com.lamdayne.humify.task.dto.response.ActivityResponse;

import java.util.List;

public interface TaskActivityService {

    List<ActivityResponse> getActivities(Long taskId);

}