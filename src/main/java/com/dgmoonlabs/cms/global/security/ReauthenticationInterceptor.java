package com.dgmoonlabs.cms.global.security;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ReauthenticationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (!(handler instanceof HandlerMethod method)
                || (!method.hasMethodAnnotation(RequiresReauthentication.class)
                && !method.getBeanType().isAnnotationPresent(RequiresReauthentication.class))) {
            return true;
        }
        LoginUser admin = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        AdminReauthentication.require(request, admin.id());
        return true;
    }
}
