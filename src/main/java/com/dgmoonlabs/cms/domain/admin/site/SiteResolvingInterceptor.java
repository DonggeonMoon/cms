package com.dgmoonlabs.cms.domain.admin.site;

import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.global.site.SiteContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 요청의 X-CMS-Site 헤더(사이트 코드)로 현재 사이트를 resolve해 {@link SiteContextHolder}에 담는다. 없으면 기본 사이트로 본다. */
@Component
@RequiredArgsConstructor
public class SiteResolvingInterceptor implements HandlerInterceptor {
    public static final String SITE_HEADER = "X-CMS-Site";

    private final ObjectProvider<SiteService> siteService;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        SiteService service = siteService.getIfAvailable();
        if (service != null) {
            SiteContextHolder.set(service.resolveSiteId(request.getHeader(SITE_HEADER)));
        }
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
                                final Exception ex) {
        SiteContextHolder.clear();
    }
}
