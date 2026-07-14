package com.lamdayne.humify.payroll.enums;

import lombok.Getter;

@Getter
public enum PayrollPeriodStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    PAID
}
