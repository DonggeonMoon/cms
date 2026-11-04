package com.dgmoonlabs.cms.global.error;

import lombok.Getter;

import java.util.List;

@Getter
public class FieldValidationException extends RuntimeException {
    private final List<ErrorResponse.FieldError> fieldErrors;

    public FieldValidationException(final List<ErrorResponse.FieldError> fieldErrors) {
        super(ErrorCode.INVALID_INPUT.getMessage());
        this.fieldErrors = List.copyOf(fieldErrors);
    }
}
