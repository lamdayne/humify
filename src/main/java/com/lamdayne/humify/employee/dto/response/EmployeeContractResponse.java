package com.lamdayne.humify.employee.dto.response;

import com.lamdayne.humify.employee.enums.ContractStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeContractResponse {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String contractNumber;
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
    private ContractStatus status;
    private String fileUrl;
    private Instant createdAt;
    private Instant updatedAt;
}