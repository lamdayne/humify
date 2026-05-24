package com.lamdayne.humify.company.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class CreateCompanyRequest implements Serializable {

    @NotBlank(message = "COMPANY_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "COMPANY_FIELD_REQUIRED")
    private String field;

    private String website;

    @NotBlank(message = "COMPANY_TAX_CODE_REQUIRED")
    private String taxCode;

    @NotBlank(message = "COMPANY_PHONE_REQUIRED")
    private String phone;

    @NotBlank(message = "COMPANY_EMAIL_REQUIRED")
    private String email;
}
