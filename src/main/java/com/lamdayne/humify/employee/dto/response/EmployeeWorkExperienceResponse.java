package com.lamdayne.humify.employee.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
@Setter
@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkExperienceResponse {

    private Long id;

    private Long employeeId;

    private String companyName;

    private String position;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String reasonForLeaving;

    private Instant createdAt;

    private Instant updatedAt;

}