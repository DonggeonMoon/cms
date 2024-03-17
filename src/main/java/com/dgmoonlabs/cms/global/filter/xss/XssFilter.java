package com.dgmoonlabs.cms.global.filter.xss;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class XssFilter implements Filter {
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        filterChain.doFilter(new XssRequestWrapper(servletRequest), servletResponse);
    }
}
