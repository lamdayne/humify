package com.lamdayne.humify.performance.dto.response;


import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemCalculatedMetricsResponse {
    private Double taskCompletionRate;
    private Double onTimeDeliveryRate;
    private Double worklogBurnedRate;
    private Double bugLeakageRate;
}

