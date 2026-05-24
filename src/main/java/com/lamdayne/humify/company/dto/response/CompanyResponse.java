package com.lamdayne.humify.company.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@Builder
public class CompanyResponse implements Serializable {
    private Long id;
    private String companyCode;
    private String name;
    private String field;
    private String website;
    private String taxCode;
    private String phone;
    private String email;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
