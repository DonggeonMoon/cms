package com.dgmoonlabs.cms.global.error;

import org.springframework.validation.BindingResult;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {
    public record FieldError(String field, String message) {
    }

    public static ErrorResponse of(final ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(final ErrorCode errorCode, final BindingResult bindingResult) {
        return new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage(),
                bindingResult.getFieldErrors().stream()
                        .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                        .toList()
        );
    }
}
