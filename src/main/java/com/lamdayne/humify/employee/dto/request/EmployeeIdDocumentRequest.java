package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class EmployeeIdDocumentRequest {

    @NotBlank(message = "ID_TYPE_REQUIRED")
    private String idType;

    @NotBlank(message = "ID_NUMBER_REQUIRED")
    private String idNumber;

    private LocalDate issuedDate;
    private String issuedPlace;
    private String expiredDate;
    private String frontImageUrl;
    private String backImageUrl;
    private Boolean current;
}