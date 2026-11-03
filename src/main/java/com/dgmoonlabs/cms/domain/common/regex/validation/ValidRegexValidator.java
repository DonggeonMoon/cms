package com.dgmoonlabs.cms.domain.common.regex.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ValidRegexValidator implements ConstraintValidator<ValidRegex, String> {
    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        try {
            Pattern.compile(value);
            return true;
        } catch (PatternSyntaxException e) {
            context.disableDefaultConstraintViolation();
            String detail = e.getDescription().replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("$", "\\$");
            context.buildConstraintViolationWithTemplate("정규식 문법이 올바르지 않습니다: " + detail).addConstraintViolation();
            return false;
        }
    }
}
