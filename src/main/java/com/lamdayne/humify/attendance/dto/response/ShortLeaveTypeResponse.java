package com.lamdayne.humify.attendance.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortLeaveTypeResponse {
    private Long id;
    private String name;
}
