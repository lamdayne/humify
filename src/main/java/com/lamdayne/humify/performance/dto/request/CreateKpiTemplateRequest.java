package com.lamdayne.humify.performance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateKpiTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @Valid
    @NotEmpty(message = "KPI items must not be empty")
    private List<KpiTemplateItemRequest> items;
}