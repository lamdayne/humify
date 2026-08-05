package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NfcSwipeRequest {

    private String employeeCode;

    private String companyCode;

    private String cardUid;

    private String deviceInfo;
}
