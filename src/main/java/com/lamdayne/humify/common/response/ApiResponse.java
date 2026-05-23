package com.lamdayne.humify.common.response;

import com.lamdayne.humify.common.exception.ErrorCode;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(SuccessCode successCode) {
        return success(successCode, null);
    }

    public static <T> ApiResponse<T> success(SuccessCode successCode, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(successCode.getCode())
                .message(successCode.getDefaultMessage())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(SuccessCode successCode, String message, T data) {
        String resolved = (message == null || message.isBlank())
                ? successCode.getDefaultMessage()
                : message;
        return ApiResponse.<T>builder()
                .success(true)
                .code(successCode.getCode())
                .message(resolved)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode, String message, T data) {
        String resolved = (message == null || message.isBlank())
                ? errorCode.getDefaultMessage()
                : message;
        return ApiResponse.<T>builder()
                .success(true)
                .code(errorCode.getCode())
                .message(resolved)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failure(ErrorCode errorCode) {
        return failure(errorCode, errorCode.getDefaultMessage(), null);
    }

}
