package com.dgmoonlabs.cms.domain.admin.account.service;

import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 관리자 API 경로의 첫 단어로 관리 영역을 찾아, 관리자 등급이 그 영역을 쓸 수 있는지 확인한다. 모르는 경로는 최고 관리자만. */
@Component
@RequiredArgsConstructor
public class AdminAreaInterceptor implements HandlerInterceptor {
    private static final String PREFIX = "/api/admin/";

    private final AdminAccountService adminAccountService;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (!path.startsWith(PREFIX)) {
            return true;
        }
        String rest = path.substring(PREFIX.length());
        int slash = rest.indexOf('/');
        String segment = slash < 0 ? rest : rest.substring(0, slash);
        LoginUser admin = CurrentUser.get().orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED));
        if (!adminAccountService.canAccess(admin.id(), segment)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return true;
    }
}
