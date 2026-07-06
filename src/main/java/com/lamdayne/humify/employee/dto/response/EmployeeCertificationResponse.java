package com.lamdayne.humify.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCertificationResponse {
    private Long id;
    private String name;
    private String certificationCode;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expiredDate;
    private String scoreOrLevel;
    private String certificationFileUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
