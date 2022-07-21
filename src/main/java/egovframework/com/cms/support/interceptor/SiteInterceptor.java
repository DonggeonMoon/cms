package egovframework.com.cms.support.interceptor;

import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.index.service.IndexService;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.site.service.SiteService;
import egovframework.com.cms.support.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SiteInterceptor implements HandlerInterceptor {
    private final IndexService indexService;
    private final SiteService siteService;
    private final ConfigOptionService configOptionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = Utils.getFixedRequestUri(request);

        log.info("SiteInterceptor requestUri : {}", requestUri);

        if (siteService == null) {
            log.info("siteService is null");
        }

        Site currentSite = siteService.detectCurrentSite(request);
        log.info("SiteInterceptor uri: {}, current site : {}", requestUri, currentSite.getSiteName());

        return true;
    }
}
