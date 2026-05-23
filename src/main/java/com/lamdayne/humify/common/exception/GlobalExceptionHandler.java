package com.lamdayne.humify.common.exception;

import com.lamdayne.humify.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.failure(e.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        FieldError fieldError = e.getFieldError();
        if (fieldError == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR));
        }

        String enumKey = fieldError.getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_ERROR_CODE;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid ErrorCode with key = \"{}\", field = \"{}\", classError = \"{}\"",
                    enumKey,
                    e.getFieldError().getField(),
                    e.getFieldError().getObjectName()
            );
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode));
    }

}
