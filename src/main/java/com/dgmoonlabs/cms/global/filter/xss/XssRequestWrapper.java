package com.dgmoonlabs.cms.global.filter.xss;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Arrays;

public class XssRequestWrapper extends HttpServletRequestWrapper {
    public XssRequestWrapper(final ServletRequest request) {
        super((HttpServletRequest) request);
    }

    @Override
    public String getHeader(final String name) {
        return sanitize(super.getHeader(name));
    }

    @Override
    public String getParameter(final String name) {
        return sanitize(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(final String name) {
        return sanitize(super.getParameterValues(name));
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return XssSanitizationRule.sanitize(value);
    }

    private String[] sanitize(String[] values) {
        if (values == null) {
            return new String[]{};
        }
        return Arrays.stream(values)
                .map(this::sanitize)
                .toArray(String[]::new);
    }
}
