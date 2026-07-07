package com.lamdayne.humify.attendance.dto.response;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LeaveTypeResponse {

    private Long id;
    private Long companyId;
    private String name;
    private String code;
    private Integer maxDays;
    private Boolean isPaid;
    private Boolean requiresAttachment;
    private String description;
}
