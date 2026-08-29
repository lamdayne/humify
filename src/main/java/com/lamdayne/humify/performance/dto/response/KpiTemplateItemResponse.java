package com.lamdayne.humify.performance.dto.response;


import com.lamdayne.humify.performance.enums.KpiMetricType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KpiTemplateItemResponse {

    private Long id;

    private String title;

    private String description;

    private KpiMetricType metricType;

    private Double targetValue;

    private String unit;

    private Double weight;
}
