package com.dgmoonlabs.cms.global.filter.xss;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
@Getter
public enum XssRule {
    A(Pattern.compile("<"), "& lt;"),
    B(Pattern.compile("\\("), "["),
    C(Pattern.compile("eval\\((.*)\\)"), ""),
    D(Pattern.compile("[\"'][\\s]*javascript:(.*)[\"']"), "\"\""),
    E(Pattern.compile("script"), "");

    private final Pattern pattern;
    private final String replacement;

    public static String sanitize(String value) {
        for (XssRule rule: values()) {
            value = rule.pattern.matcher(value).replaceAll(rule.replacement);
        }
        return value;
    }
}
