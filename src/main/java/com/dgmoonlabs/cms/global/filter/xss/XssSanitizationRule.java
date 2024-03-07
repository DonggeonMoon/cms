package com.dgmoonlabs.cms.global.filter.xss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
public enum XssSanitizationRule {
    LT(Pattern.compile("<"), "& lt;"),
    PARENTHESIS(Pattern.compile("\\("), "["),
    EVAL(Pattern.compile("eval\\((.*)\\)"), ""),
    JAVASCRIPT_SCHEME(Pattern.compile("[\"'][\\s]*javascript:(.*)[\"']"), "\"\""),
    SCRIPT(Pattern.compile("script"), "");

    private final Pattern pattern;
    private final String replacement;

    public static String sanitize(String value) {
        for (XssSanitizationRule rule: values()) {
            value = rule.pattern.matcher(value).replaceAll(rule.replacement);
        }
        return value;
    }
}
