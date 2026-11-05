package com.dgmoonlabs.cms.domain.admin.ipaccess.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminIpInterceptor implements HandlerInterceptor {
    private final AdminIpAccessService adminIpAccessService;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        adminIpAccessService.requireAllowed(request.getRemoteAddr());
        return true;
    }
}
