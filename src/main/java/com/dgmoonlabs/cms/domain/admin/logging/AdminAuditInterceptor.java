package com.dgmoonlabs.cms.domain.admin.logging;

import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuditInterceptor implements HandlerInterceptor {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) {
        boolean read = HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod());
        if (read && !(handler instanceof HandlerMethod method && method.hasMethodAnnotation(AuditedRead.class))) {
            return;
        }
        String handlerName = handler instanceof HandlerMethod method
                ? method.getBeanType().getSimpleName() + "#" + method.getMethod().getName()
                : handler.getClass().getSimpleName();

        eventPublisher.publishEvent(new AdminAuditEvent(
                currentUsername(),
                request.getMethod() + " " + request.getRequestURI(),
                handlerName,
                "status=" + response.getStatus(),
                request.getRemoteAddr()
        ));
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.username();
        }
        return authentication == null ? "anonymous" : authentication.getName();
    }

    public record AdminAuditEvent(String username, String uri, String handler, String content, String ipAddress) {
    }
}
