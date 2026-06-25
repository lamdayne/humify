package com.lamdayne.humify.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSprintRequest {

    @NotBlank(message = "SPRINT_NAME_REQUIRED")
    private String name;
    private String goal;
    private Instant startDate;
    private Instant endDate;

}
