package com.dgmoonlabs.cms.domain.admin.site;

import com.dgmoonlabs.cms.domain.admin.site.constant.SiteFeature;
import com.dgmoonlabs.cms.domain.admin.site.service.SiteService;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SiteFeatureInterceptor implements HandlerInterceptor {
    private final ObjectProvider<SiteService> siteService;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        String path = request.getRequestURI();
        List<SiteFeature> features = SiteFeature.ofPath(path);
        if (features.isEmpty() || (CurrentUser.isAdmin() && !path.startsWith("/api/admin/"))) {
            return true;
        }
        SiteService service = siteService.getIfAvailable();
        if (service != null && features.stream().noneMatch(service::isEnabled)) {
            throw new BusinessException(ErrorCode.FEATURE_DISABLED);
        }
        return true;
    }
}
