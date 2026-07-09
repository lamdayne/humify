package com.lamdayne.humify.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateLeaveBalanceRequest {
    @NotNull(message = "LEAVE_TYPE_ID_REQUIRED")
    private Long leaveTypeId;

    @NotNull(message = "YEAR_REQUIRED")
    private Integer year;

    @NotNull(message = "ALLOCATED_DAYS_REQUIRED")
    @PositiveOrZero(message = "ALLOCATED_DAYS_INVALID")
    private BigDecimal allocatedDays;
}