package com.dgmoonlabs.cms.domain.common.regex.validation;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import com.dgmoonlabs.cms.domain.common.regex.service.RegexService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class MatchesRegexValidator implements ConstraintValidator<MatchesRegex, String> {
    private final RegexService regexService;
    private RegexTarget key;

    @Override
    public void initialize(final MatchesRegex annotation) {
        this.key = annotation.value();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        Optional<String> violation = regexService.violation(key, value);
        if (violation.isEmpty()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(escape(violation.get())).addConstraintViolation();
        return false;
    }

    // 관리자가 쓴 메시지의 {, $ 가 메시지 보간식으로 해석되지 않게 한다.
    private static String escape(final String message) {
        return message.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}").replace("$", "\\$");
    }
}
