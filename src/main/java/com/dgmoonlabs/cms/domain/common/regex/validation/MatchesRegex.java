package com.dgmoonlabs.cms.domain.common.regex.validation;

import com.dgmoonlabs.cms.domain.common.regex.constant.RegexTarget;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchesRegexValidator.class)
public @interface MatchesRegex {
    RegexTarget value();

    String message() default "형식이 올바르지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
