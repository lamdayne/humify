package com.lamdayne.humify.employee.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class EmployeeIdDocumentResponse {
    private Long id;
    private Long employeeId;
    private String idType;
    private String idNumber;
    private LocalDate issuedDate;
    private String issuedPlace;
    private LocalDate expiredDate;
    private Boolean current;
    private String frontImageUrl;
    private String backImageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}