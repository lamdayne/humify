package com.lamdayne.humify.employee.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateContractRequest {
    private String contractType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal baseSalary;
    private BigDecimal allowanceLunch;
    private BigDecimal allowancePhone;
    private BigDecimal allowanceTransport;
    private BigDecimal allowanceOther;
    private BigDecimal insuranceSalary;
    private Integer taxableDependents;
    private String status;
    private String fileUrl;
}