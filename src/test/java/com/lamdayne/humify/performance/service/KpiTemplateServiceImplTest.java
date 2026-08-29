package com.lamdayne.humify.performance.service;

import com.lamdayne.humify.auth.security.principal.UserPrincipal;
import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.performance.dto.request.CreateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.request.KpiTemplateItemRequest;
import com.lamdayne.humify.performance.dto.request.UpdateKpiTemplateRequest;
import com.lamdayne.humify.performance.dto.response.KpiTemplateResponse;
import com.lamdayne.humify.performance.entity.KpiTemplate;
import com.lamdayne.humify.performance.entity.KpiTemplateItem;
import com.lamdayne.humify.performance.enums.KpiMetricType;
import com.lamdayne.humify.performance.mapper.KpiTemplateMapper;
import com.lamdayne.humify.performance.repository.KpiTemplateRepository;
import com.lamdayne.humify.performance.service.impl.KpiTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KpiTemplateServiceImpl Tests")
class KpiTemplateServiceImplTest {

    @Mock
    private KpiTemplateRepository kpiTemplateRepository;

    @Mock
    private KpiTemplateMapper kpiTemplateMapper;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private KpiTemplateServiceImpl kpiTemplateService;

    private static final Long COMPANY_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long TEMPLATE_ID = 100L;

    private KpiTemplate template;
    private KpiTemplateResponse response;

    @BeforeEach
    void setUp() {

        template = new KpiTemplate();

        ReflectionTestUtils.setField(
                template,
                "id",
                TEMPLATE_ID
        );

        template.setCompanyId(COMPANY_ID);
        template.setCreatedBy(USER_ID);
        template.setName(
                "Developer KPI"
        );
        template.setDescription(
                "Developer performance KPI"
        );
        template.setIsActive(true);
        template.setItems(
                new ArrayList<>()
        );

        response =
                new KpiTemplateResponse();

        response.setId(TEMPLATE_ID);
        response.setName(
                "Developer KPI"
        );
    }


    // =====================================================
    // CREATE TEMPLATE
    // =====================================================

    @Test
    @DisplayName(
            "Create KPI template successfully"
    )
    void createTemplate_success() {

        CreateKpiTemplateRequest request =
                new CreateKpiTemplateRequest();

        request.setName(
                "Developer KPI"
        );

        request.setDescription(
                "Developer template"
        );

        request.setItems(
                List.of(
                        createItem(
                                "Completion Rate",
                                KpiMetricType.TASK_COMPLETION_RATE,
                                80.0,
                                "%",
                                60.0
                        ),
                        createItem(
                                "On-time Rate",
                                KpiMetricType.TASK_ON_TIME_RATE,
                                90.0,
                                "%",
                                40.0
                        )
                )
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                userPrincipal.getId()
        ).thenReturn(USER_ID);

        when(
                kpiTemplateRepository
                        .existsByCompanyIdAndNameIgnoreCaseAndDeletedAtIsNull(
                                COMPANY_ID,
                                "Developer KPI"
                        )
        ).thenReturn(false);

        when(
                kpiTemplateRepository.save(
                        any(KpiTemplate.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                kpiTemplateMapper.toResponse(
                        any(KpiTemplate.class)
                )
        ).thenReturn(response);


        KpiTemplateResponse result =
                kpiTemplateService
                        .createTemplate(
                                userPrincipal,
                                request
                        );


        assertThat(result)
                .isSameAs(response);

        ArgumentCaptor<KpiTemplate> captor =
                ArgumentCaptor.forClass(
                        KpiTemplate.class
                );

        verify(
                kpiTemplateRepository
        ).save(
                captor.capture()
        );

        KpiTemplate saved =
                captor.getValue();

        assertThat(
                saved.getCompanyId()
        ).isEqualTo(COMPANY_ID);

        assertThat(
                saved.getCreatedBy()
        ).isEqualTo(USER_ID);

        assertThat(
                saved.getName()
        ).isEqualTo(
                "Developer KPI"
        );

        assertThat(
                saved.getIsActive()
        ).isTrue();

        assertThat(
                saved.getItems()
        ).hasSize(2);

        assertThat(
                saved.getItems()
                        .get(0)
                        .getTemplate()
        ).isSameAs(saved);
    }


    @Test
    @DisplayName(
            "Create template throws exception when item list is null"
    )
    void createTemplate_throwsWhenItemsNull() {

        CreateKpiTemplateRequest request =
                new CreateKpiTemplateRequest();

        request.setName("Test");
        request.setItems(null);

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                userPrincipal.getId()
        ).thenReturn(USER_ID);


        assertThatThrownBy(() ->
                kpiTemplateService
                        .createTemplate(
                                userPrincipal,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_ITEMS_EMPTY
                        )
                );

        verify(
                kpiTemplateRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Create template throws exception when item list is empty"
    )
    void createTemplate_throwsWhenItemsEmpty() {

        CreateKpiTemplateRequest request =
                new CreateKpiTemplateRequest();

        request.setName("Test");

        request.setItems(
                Collections.emptyList()
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                userPrincipal.getId()
        ).thenReturn(USER_ID);


        assertThatThrownBy(() ->
                kpiTemplateService
                        .createTemplate(
                                userPrincipal,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_ITEMS_EMPTY
                        )
                );
    }


    @Test
    @DisplayName(
            "Create template throws exception when total weight is invalid"
    )
    void createTemplate_throwsWhenWeightInvalid() {

        CreateKpiTemplateRequest request =
                new CreateKpiTemplateRequest();

        request.setName("Test");

        request.setItems(
                List.of(
                        createItem(
                                "KPI 1",
                                KpiMetricType.MANUAL,
                                100.0,
                                "%",
                                60.0
                        ),
                        createItem(
                                "KPI 2",
                                KpiMetricType.MANUAL,
                                100.0,
                                "%",
                                30.0
                        )
                )
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                userPrincipal.getId()
        ).thenReturn(USER_ID);


        assertThatThrownBy(() ->
                kpiTemplateService
                        .createTemplate(
                                userPrincipal,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_WEIGHT_INVALID
                        )
                );

        verify(
                kpiTemplateRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Create template throws exception when template name already exists"
    )
    void createTemplate_throwsWhenNameExists() {

        CreateKpiTemplateRequest request =
                new CreateKpiTemplateRequest();

        request.setName(
                "Developer KPI"
        );

        request.setItems(
                List.of(
                        createItem(
                                "KPI",
                                KpiMetricType.MANUAL,
                                100.0,
                                "%",
                                100.0
                        )
                )
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                userPrincipal.getId()
        ).thenReturn(USER_ID);

        when(
                kpiTemplateRepository
                        .existsByCompanyIdAndNameIgnoreCaseAndDeletedAtIsNull(
                                COMPANY_ID,
                                "Developer KPI"
                        )
        ).thenReturn(true);


        assertThatThrownBy(() ->
                kpiTemplateService
                        .createTemplate(
                                userPrincipal,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_NAME_EXISTED
                        )
                );

        verify(
                kpiTemplateRepository,
                never()
        ).save(any());
    }


    // =====================================================
    // GET ALL
    // =====================================================

    @Test
    @DisplayName(
            "Get all KPI templates successfully"
    )
    void getAllTemplates_success() {

        KpiTemplate template2 =
                new KpiTemplate();

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findAllByCompanyIdAndDeletedAtIsNull(
                                COMPANY_ID
                        )
        ).thenReturn(
                List.of(
                        template,
                        template2
                )
        );

        KpiTemplateResponse response2 =
                new KpiTemplateResponse();

        when(
                kpiTemplateMapper.toResponse(template)
        ).thenReturn(response);

        when(
                kpiTemplateMapper.toResponse(template2)
        ).thenReturn(response2);


        List<KpiTemplateResponse> result =
                kpiTemplateService
                        .getAllTemplates(
                                userPrincipal
                        );


        assertThat(result)
                .containsExactly(
                        response,
                        response2
                );
    }


    @Test
    @DisplayName(
            "Get all KPI templates returns empty list"
    )
    void getAllTemplates_empty() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findAllByCompanyIdAndDeletedAtIsNull(
                                COMPANY_ID
                        )
        ).thenReturn(
                Collections.emptyList()
        );


        List<KpiTemplateResponse> result =
                kpiTemplateService
                        .getAllTemplates(
                                userPrincipal
                        );


        assertThat(result)
                .isEmpty();

        verifyNoInteractions(
                kpiTemplateMapper
        );
    }


    // =====================================================
    // GET BY ID
    // =====================================================

    @Test
    @DisplayName(
            "Get KPI template by ID successfully"
    )
    void getTemplateById_success() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                kpiTemplateMapper.toResponse(template)
        ).thenReturn(response);


        KpiTemplateResponse result =
                kpiTemplateService
                        .getTemplateById(
                                userPrincipal,
                                TEMPLATE_ID
                        );


        assertThat(result)
                .isSameAs(response);
    }


    @Test
    @DisplayName(
            "Get KPI template by ID throws when not found"
    )
    void getTemplateById_throwsWhenNotFound() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(() ->
                kpiTemplateService
                        .getTemplateById(
                                userPrincipal,
                                TEMPLATE_ID
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_NOT_FOUND
                        )
                );

        verifyNoInteractions(
                kpiTemplateMapper
        );
    }


    // =====================================================
    // UPDATE
    // =====================================================

    @Test
    @DisplayName(
            "Update KPI template successfully"
    )
    void updateTemplate_success() {

        KpiTemplateItem oldItem =
                new KpiTemplateItem();

        template.addItem(oldItem);

        UpdateKpiTemplateRequest request =
                new UpdateKpiTemplateRequest();

        request.setName(
                "Updated KPI"
        );

        request.setDescription(
                "Updated description"
        );

        request.setIsActive(false);

        request.setItems(
                List.of(
                        createItem(
                                "New KPI",
                                KpiMetricType.TASK_COMPLETION_RATE,
                                90.0,
                                "%",
                                100.0
                        )
                )
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                kpiTemplateRepository
                        .existsByCompanyIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                                COMPANY_ID,
                                "Updated KPI",
                                TEMPLATE_ID
                        )
        ).thenReturn(false);

        when(
                kpiTemplateRepository.save(template)
        ).thenReturn(template);

        when(
                kpiTemplateMapper.toResponse(template)
        ).thenReturn(response);


        KpiTemplateResponse result =
                kpiTemplateService
                        .updateTemplate(
                                userPrincipal,
                                TEMPLATE_ID,
                                request
                        );


        assertThat(result)
                .isSameAs(response);

        assertThat(
                template.getName()
        ).isEqualTo(
                "Updated KPI"
        );

        assertThat(
                template.getDescription()
        ).isEqualTo(
                "Updated description"
        );

        assertThat(
                template.getIsActive()
        ).isFalse();

        assertThat(
                template.getItems()
        ).hasSize(1);

        assertThat(
                template.getItems()
                        .get(0)
                        .getTitle()
        ).isEqualTo(
                "New KPI"
        );

        assertThat(
                template.getItems()
                        .get(0)
                        .getTemplate()
        ).isSameAs(template);
    }


    @Test
    @DisplayName(
            "Update template keeps active state when isActive is null"
    )
    void updateTemplate_isActiveNull_shouldKeepCurrentValue() {

        template.setIsActive(true);

        UpdateKpiTemplateRequest request =
                new UpdateKpiTemplateRequest();

        request.setName("Updated KPI");
        request.setDescription("Description");
        request.setIsActive(null);

        request.setItems(
                List.of(
                        createItem(
                                "KPI",
                                KpiMetricType.MANUAL,
                                100.0,
                                "%",
                                100.0
                        )
                )
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                kpiTemplateRepository
                        .existsByCompanyIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                                COMPANY_ID,
                                "Updated KPI",
                                TEMPLATE_ID
                        )
        ).thenReturn(false);

        when(
                kpiTemplateRepository.save(template)
        ).thenReturn(template);

        when(
                kpiTemplateMapper.toResponse(template)
        ).thenReturn(response);


        kpiTemplateService.updateTemplate(
                userPrincipal,
                TEMPLATE_ID,
                request
        );


        assertThat(
                template.getIsActive()
        ).isTrue();
    }


    @Test
    @DisplayName(
            "Update template throws when template does not exist"
    )
    void updateTemplate_throwsWhenNotFound() {

        UpdateKpiTemplateRequest request =
                new UpdateKpiTemplateRequest();

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(() ->
                kpiTemplateService
                        .updateTemplate(
                                userPrincipal,
                                TEMPLATE_ID,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_NOT_FOUND
                        )
                );
    }


    @Test
    @DisplayName(
            "Update template throws when name already exists"
    )
    void updateTemplate_throwsWhenNameExists() {

        UpdateKpiTemplateRequest request =
                new UpdateKpiTemplateRequest();

        request.setName("Existing KPI");

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                kpiTemplateRepository
                        .existsByCompanyIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                                COMPANY_ID,
                                "Existing KPI",
                                TEMPLATE_ID
                        )
        ).thenReturn(true);


        assertThatThrownBy(() ->
                kpiTemplateService
                        .updateTemplate(
                                userPrincipal,
                                TEMPLATE_ID,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_NAME_EXISTED
                        )
                );

        verify(
                kpiTemplateRepository,
                never()
        ).save(any());
    }


    @Test
    @DisplayName(
            "Update template throws when weight is invalid"
    )
    void updateTemplate_throwsWhenWeightInvalid() {

        UpdateKpiTemplateRequest request =
                new UpdateKpiTemplateRequest();

        request.setName(
                "Developer KPI"
        );

        request.setItems(
                List.of(
                        createItem(
                                "KPI",
                                KpiMetricType.MANUAL,
                                100.0,
                                "%",
                                50.0
                        )
                )
        );

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );

        when(
                kpiTemplateRepository
                        .existsByCompanyIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                                COMPANY_ID,
                                "Developer KPI",
                                TEMPLATE_ID
                        )
        ).thenReturn(false);


        assertThatThrownBy(() ->
                kpiTemplateService
                        .updateTemplate(
                                userPrincipal,
                                TEMPLATE_ID,
                                request
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_WEIGHT_INVALID
                        )
                );

        verify(
                kpiTemplateRepository,
                never()
        ).save(any());
    }


    // =====================================================
    // DELETE
    // =====================================================

    @Test
    @DisplayName(
            "Delete KPI template should soft delete template"
    )
    void deleteTemplate_success() {

        template.setIsActive(true);
        template.setDeletedAt(null);

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.of(template)
        );


        kpiTemplateService
                .deleteTemplate(
                        userPrincipal,
                        TEMPLATE_ID
                );


        assertThat(
                template.getIsActive()
        ).isFalse();

        assertThat(
                template.getDeletedAt()
        ).isNotNull();

        verify(
                kpiTemplateRepository
        ).save(template);
    }


    @Test
    @DisplayName(
            "Delete template throws when template not found"
    )
    void deleteTemplate_throwsWhenNotFound() {

        when(
                userPrincipal.getCompanyId()
        ).thenReturn(COMPANY_ID);

        when(
                kpiTemplateRepository
                        .findByIdAndCompanyIdAndDeletedAtIsNull(
                                TEMPLATE_ID,
                                COMPANY_ID
                        )
        ).thenReturn(
                Optional.empty()
        );


        assertThatThrownBy(() ->
                kpiTemplateService
                        .deleteTemplate(
                                userPrincipal,
                                TEMPLATE_ID
                        )
        )
                .isInstanceOf(
                        AppException.class
                )
                .satisfies(ex ->
                        assertThat(
                                ((AppException) ex)
                                        .getErrorCode()
                        ).isEqualTo(
                                ErrorCode
                                        .KPI_TEMPLATE_NOT_FOUND
                        )
                );

        verify(
                kpiTemplateRepository,
                never()
        ).save(any());
    }


    // =====================================================
    // HELPERS
    // =====================================================

    private KpiTemplateItemRequest createItem(
            String title,
            KpiMetricType metricType,
            Double target,
            String unit,
            Double weight
    ) {

        KpiTemplateItemRequest item =
                new KpiTemplateItemRequest();

        item.setTitle(title);
        item.setDescription(
                title + " description"
        );
        item.setMetricType(metricType);
        item.setTargetValue(target);
        item.setUnit(unit);
        item.setWeight(weight);

        return item;
    }
}