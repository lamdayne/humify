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
public class WorkloadResponse {
    private Long userId;
    private String userName;
    private String email;
    private String avatar;
    private long count;
    private double percentage;
}
