package com.lamdayne.humify.company.dto.request;

import com.lamdayne.humify.common.validator.EmailPattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class UpdateCompanyRequest implements Serializable {

    @NotBlank(message = "COMPANY_NAME_REQUIRED")
    private String name;

    @NotBlank(message = "COMPANY_FIELD_REQUIRED")
    private String field;

    private String website;

    @Size(max = 20, message = "COMPANY_PHONE_INVALID")
    @NotBlank(message = "COMPANY_PHONE_REQUIRED")
    private String phone;

    @EmailPattern(message = "COMPANY_EMAIL_INVALID")
    @NotBlank(message = "COMPANY_EMAIL_REQUIRED")
    private String email;

}
