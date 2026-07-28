package com.lamdayne.humify.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiMetricsResponse {
    private long completedLast7Days;
    private long updatedLast7Days;
    private long createdLast7Days;
    private long dueSoonNext7Days;
}
