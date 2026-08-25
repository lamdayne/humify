package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.performance.dto.request.CreateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.response.KpiTemplateResponse;

import java.util.List;

public interface KpiTemplateService {
    KpiTemplateResponse createTemplate(
            UserPrincipal userPrincipal,
            CreateKpiTemplateRequest request
    );

    List<KpiTemplateResponse> getAllTemplates(
            UserPrincipal userPrincipal
    );

    KpiTemplateResponse getTemplateById(
            UserPrincipal userPrincipal,
            Long templateId
    );

    KpiTemplateResponse updateTemplate(
            UserPrincipal userPrincipal,
            Long templateId,
            UpdateKpiTemplateRequest request
    );

    void deleteTemplate(
            UserPrincipal userPrincipal,
            Long templateId
    );

}
