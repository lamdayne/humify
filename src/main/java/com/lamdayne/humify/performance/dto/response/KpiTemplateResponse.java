package com.lamdayne.humify.performance.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
public class KpiTemplateResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean isActive;

    private List<KpiTemplateItemResponse> items;

    private  Instant createdAt;

    private Instant updatedAt;
}
