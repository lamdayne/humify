package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UpdateEmployeeCertificationRequest {
    @NotBlank(message = "EMPLOYEE_CERTIFICATION_NAME_REQUIRED")
    private String name;
    private String certificationCode;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate expiredDate;
    private String scoreOrLevel;
    private String certificationFileUrl;
}
