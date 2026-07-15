package com.lamdayne.humify.common.exception;

import com.lamdayne.humify.common.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        return buildErrorResponse(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        FieldError fieldError = e.getFieldError();
        if (fieldError == null) {
            return buildErrorResponse(ErrorCode.VALIDATION_ERROR);
        }

        ErrorCode errorCode = resolveErrorCode(
                fieldError.getDefaultMessage(),
                fieldError.getField(),
                fieldError.getObjectName()
        );

        return buildErrorResponse(errorCode);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException e
    ) {
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);

        if (violation == null) {
            return buildErrorResponse(ErrorCode.VALIDATION_ERROR);
        }

        String enumKey = violation.getMessage();
        String field = violation.getPropertyPath().toString();

        ErrorCode errorCode = resolveErrorCode(
                enumKey,
                field,
                violation.getRootBeanClass().getSimpleName()
        );

        return buildErrorResponse(errorCode);
    }

    private ErrorCode resolveErrorCode(String enumKey, String field, String objectName) {
        try {
            return ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid ErrorCode with key = \"{}\", field = \"{}\", classError = \"{}\"",
                    enumKey,
                    field,
                    objectName
            );
        }
        return ErrorCode.INVALID_ERROR_CODE;
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Void>> handlePropertyReferenceException(
            PropertyReferenceException e
    ) {
        return buildErrorResponse(ErrorCode.INVALID_FIELD_NAME);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException() {
        return buildErrorResponse(ErrorCode.JWT_EXPIRED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException() {
        return buildErrorResponse(ErrorCode.REQUEST_BODY_MISSING_OR_INVALID);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ) {
        return buildErrorResponse(ErrorCode.INVALID_DATA);
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode));
    }

}
