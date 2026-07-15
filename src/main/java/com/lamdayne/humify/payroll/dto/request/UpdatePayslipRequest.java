package com.lamdayne.humify.payroll.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayslipRequest {
    @DecimalMin(value = "0.0", message = "otherBonuses không được âm")
    private BigDecimal otherBonuses;

    @DecimalMin(value = "0.0", message = "otherDeductions không được âm")
    private BigDecimal otherDeductions;

    private String note;
}
