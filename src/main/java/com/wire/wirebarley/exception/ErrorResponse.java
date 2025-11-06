package com.wire.wirebarley.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp,
        List<FieldError> fieldErrors
) {
    public ErrorResponse(String code, String message) {
        this(code, message, LocalDateTime.now(), null);
    }

    public ErrorResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), LocalDateTime.now(), null);
    }

    public ErrorResponse(String code, String message, List<FieldError> fieldErrors) {
        this(code, message, LocalDateTime.now(), fieldErrors);
    }

    public record FieldError(
            String field,
            String message
    ) {
    }
}