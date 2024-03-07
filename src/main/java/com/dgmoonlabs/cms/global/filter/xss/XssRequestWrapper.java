package com.dgmoonlabs.cms.global.filter.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Arrays;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(final HttpServletRequest request) {
        super(request);
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
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
                .map(this::sanitize)
                .toArray(String[]::new);
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return XssSanitizationRule.sanitize(value);
    }
}
