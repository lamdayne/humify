package com.lamdayne.humify.attendance.dto.request;

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
