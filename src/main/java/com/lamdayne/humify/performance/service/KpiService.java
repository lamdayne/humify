package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.performance.dto.request.CreateKpiRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiProgressRequest;
import com.lamdayne.humify.performance.dto.response.KpiResponse;

import java.time.LocalDate;
import java.util.List;

public interface KpiService {
    KpiResponse createKpi(Long employeeId, CreateKpiRequest request);
    List<KpiResponse> getKpisByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate);
    KpiResponse updateKpi(Long id, CreateKpiRequest request);
    KpiResponse updateProgress(Long id, UpdateKpiProgressRequest request);
    void deleteKpi(Long id);
}