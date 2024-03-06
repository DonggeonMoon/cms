package com.dgmoonlabs.cms.global.filter.xss;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

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

        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = sanitize(values[i]);
        }
        return encodedValues;
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return XssRule.sanitize(value);
    }
}
