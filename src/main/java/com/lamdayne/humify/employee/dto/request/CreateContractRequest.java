package com.lamdayne.humify.employee.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateContractRequest {

    @NotNull(message = "EMPLOYEE_ID_REQUIRED")
    private Long employeeId;

    @NotBlank(message = "CONTRACT_NUMBER_REQUIRED")
    private String contractNumber;

    @NotBlank(message = "CONTRACT_TYPE_REQUIRED")
    private String contractType;

    @NotNull(message = "START_DATE_REQUIRED")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "BASE_SALARY_REQUIRED")
    @PositiveOrZero(message = "BASE_SALARY_INVALID")
    private BigDecimal baseSalary;

    @PositiveOrZero(message = "ALLOWANCE_LUNCH_INVALID")
    private BigDecimal allowanceLunch;

    @PositiveOrZero(message = "ALLOWANCE_PHONE_INVALID")
    private BigDecimal allowancePhone;

    @PositiveOrZero(message = "ALLOWANCE_TRANSPORT_INVALID")
    private BigDecimal allowanceTransport;

    @PositiveOrZero(message = "ALLOWANCE_OTHER_INVALID")
    private BigDecimal allowanceOther;

    @NotNull(message = "INSURANCE_SALARY_REQUIRED")
    @PositiveOrZero(message = "INSURANCE_SALARY_INVALID")
    private BigDecimal insuranceSalary;

    @Min(value = 0, message = "TAXABLE_DEPENDENTS_INVALID")
    private Integer taxableDependents;

    private String fileUrl;
}