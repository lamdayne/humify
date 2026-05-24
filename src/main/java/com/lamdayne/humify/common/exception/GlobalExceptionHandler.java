package com.lamdayne.humify.common.exception;

import com.lamdayne.humify.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode));
    }

}
