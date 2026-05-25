package com.lamdayne.humify.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorCode {

    // Common
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    INVALID_ERROR_CODE("INVALID_ERROR_CODE", "Invalid error code", HttpStatus.BAD_REQUEST),
    PAGE_NO_INVALID("PAGE_NO_INVALID", "Invalid page number", HttpStatus.BAD_REQUEST),
    PAGE_SIZE_INVALID("PAGE_SIZE_INVALID", "Invalid page size", HttpStatus.BAD_REQUEST),

    // Company
    COMPANY_NAME_REQUIRED("COMPANY_NAME_REQUIRED", "Company name can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_FIELD_REQUIRED("COMPANY_FIELD_REQUIRED", "Company field can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_TAX_CODE_REQUIRED("COMPANY_TAX_CODE_REQUIRED", "Company tax code can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_PHONE_REQUIRED("COMPANY_PHONE_REQUIRED", "Company phone can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_EMAIL_REQUIRED("COMPANY_EMAIL_REQUIRED", "Company email can not blank", HttpStatus.BAD_REQUEST),
    COMPANY_TAX_CODE_INVALID("COMPANY_TAX_CODE_INVALID", "Company tax code is invalid", HttpStatus.BAD_REQUEST),
    COMPANY_TAX_CODE_EXISTED("COMPANY_TAX_CODE_EXISTED", "Company tax code already exists", HttpStatus.BAD_REQUEST),
    COMPANY_PHONE_INVALID("COMPANY_PHONE_INVALID", "Company phone number is invalid", HttpStatus.BAD_REQUEST),
    COMPANY_EMAIL_INVALID("COMPANY_EMAIL_INVALID", "Company email is invalid", HttpStatus.BAD_REQUEST),
    //Department
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Branch not found" , HttpStatus.BAD_REQUEST ),
    BRANCH_ID_REQUIRED("BRANCH_ID_REQUIRED", "branch id can not blank", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NAME_REQUIRED("BRANCH_NOT_FOUND", "Department name can not blank", HttpStatus.BAD_REQUEST),;
    private String code;
    private String defaultMessage;
    private HttpStatus status;
}
