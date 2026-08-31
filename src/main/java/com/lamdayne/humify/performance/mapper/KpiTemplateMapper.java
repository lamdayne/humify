package com.lamdayne.humify.performance.mapper;

import com.lamdayne.humify.performance.dto.request.CreateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.request.KpiTemplateItemRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.response.KpiTemplateItemResponse;
import com.lamdayne.humify.performance.dto.response.KpiTemplateResponse;
import com.lamdayne.humify.performance.entity.KpiTemplate;
import com.lamdayne.humify.performance.entity.KpiTemplateItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface KpiTemplateMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    KpiTemplate toEntity(
            CreateKpiTemplateRequest request
    );


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "template", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    KpiTemplateItem toItemEntity(
            KpiTemplateItemRequest request
    );


    KpiTemplateResponse toResponse(
            KpiTemplate entity
    );


    KpiTemplateItemResponse toItemResponse(
            KpiTemplateItem entity
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "companyId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(
            UpdateKpiTemplateRequest request,
            @MappingTarget KpiTemplate entity
    );
}