package com.lamdayne.humify.project.dto.response;

import com.lamdayne.humify.project.enums.SprintStatus;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintResponse {

    private Long id;
    private Long projectId;
    private String name;
    private String goal;
    private Instant startDate;
    private Instant endDate;
    private SprintStatus status;
    private Instant createdAt;
    private Instant updatedAt;

}
