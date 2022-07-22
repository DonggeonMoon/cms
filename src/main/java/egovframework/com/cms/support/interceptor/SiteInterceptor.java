package egovframework.com.cms.support.interceptor;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.mail.service.impl.MailServiceImpl;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.site.service.SiteService;
import egovframework.com.cms.support.util.Utils;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class SiteInterceptor implements HandlerInterceptor {
    private final EntityManager entityManager;
    private final SiteService siteService;
    private final ConfigOptionService configOptionService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull Object handler) throws IOException {
        String requestUri = Utils.getFixedRequestUri(request);
        log.info("SiteInterceptor requestUri: {}", requestUri);

        Site currentSite = siteService.detectCurrentSite(request);
        log.info("SiteInterceptor uri: {}, current site: {}", requestUri, currentSite.getSiteName());

        List<Site> siteList = siteService.getSiteList(null);
        request.setAttribute("siteList", siteList);
        request.setAttribute("currentSite", currentSite);

        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("X-Content-Type-Options", "nosniff");

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setDateHeader("Expires", -1);

        Cookie langManualCookie;
        if (!requestUri.startsWith(Utils.getAppPath(currentSite) + "/file/") && !requestUri.endsWith(".do") &&
                "manual".equals(request.getParameter("lang"))
        ) {
            langManualCookie = new Cookie("lang", "manual");
            langManualCookie.setPath("/");
            langManualCookie.setHttpOnly(true);
            response.addCookie(langManualCookie);
        }

        if (!requestUri.startsWith(Utils.getAdminPath(currentSite))) {
            Locale locale = LocaleUtils.toLocale(currentSite.getSiteLocale());
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
        }

        if ("true".equals(request.getParameter("flush"))) {
            entityManager.flush();
        }

        Config globalConfig = configOptionService.getConfig("global");
        request.setAttribute("globalConfig", globalConfig);
        request.setAttribute("globalConfigMap", globalConfig.getOptionMap());

        Config siteConfig = configOptionService.getConfig("site");
        request.setAttribute("siteConfig", siteConfig);
        request.setAttribute("siteConfigMap", siteConfig.getOptionMap());

        Config snsConfig = configOptionService.getConfig("sns");
        request.setAttribute("snsConfig", snsConfig);
        request.setAttribute("snsConfigMap", snsConfig.getOptionMap());

        Config commentConfig = configOptionService.getConfig("comment");
        request.setAttribute("commentConfig", commentConfig);
        request.setAttribute("commentConfigMap", commentConfig.getOptionMap());

        Config securityConfig = configOptionService.getConfig("security");
        request.setAttribute("securityConfig", securityConfig);
        request.setAttribute("securityConfigMap", securityConfig.getOptionMap());

        Config designConfig = configOptionService.getConfig("static/design");
        request.setAttribute("designConfig", designConfig);
        request.setAttribute("designConfigMap", designConfig.getOptionMap());

        Config mediaConfig = configOptionService.getConfig("media");
        request.setAttribute("mediaConfig", mediaConfig);
        request.setAttribute("mediaConfigMap", mediaConfig);

        Config remotePublishConfig = configOptionService.getConfig("remote_publish");
        request.setAttribute("remotePublishConfig", remotePublishConfig);
        request.setAttribute("remotePublishConfigMap", remotePublishConfig.getOptionMap());

        Config watermarkConfig = configOptionService.getConfig("watermark");
        request.setAttribute("watermarkConfig", watermarkConfig);
        request.setAttribute("watermarkConfigMap", watermarkConfig.getOptionMap());

        Config gaConfig = configOptionService.getConfig("google_analytics");
        request.setAttribute("gaConfig", gaConfig);
        request.setAttribute("gaConfigMap", gaConfig.getOptionMap());

        if (StringUtils.isBlank(MailServiceImpl.schemeDomainPort) ||
                !StringUtils.equals(MailServiceImpl.schemeDomainPort, Utils.getSchemeDomainPort(request))
        ) {
            MailServiceImpl.schemeDomainPort = Utils.getSchemeDomainPort(request);
        }

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        request.setAttribute("userAgent", userAgent);

        if (!requestUri.startsWith(Utils.getAdminPath(currentSite)) && !requestUri.contains("logout.do") &&
                "true".equals(siteConfig.getOption("force_redirect")) &&
                StringUtils.isNotBlank(siteConfig.getOption("force_redirect_url"))) {
            log.info("SiteInterceptor FORCE REDIRECT: {}", siteConfig.getOption("force_redirect_url"));
            response.sendRedirect((siteConfig.getOption("force_redirect_url")));
            return false;
        }

        Cookie deviceType = Utils.getCookie(request.getCookies(), "deviceType");

        if (!Utils.isFilePath(requestUri) && StringUtils.isNotBlank(request.getParameter("deviceType"))) {
            deviceType = new Cookie("deviceType", request.getParameter("deviceType"));
            deviceType.setPath("/");
            deviceType.setHttpOnly(true);
            response.addCookie(deviceType);
        }

        if (!Utils.isFilePath(requestUri) &&
                userAgent.getOperatingSystem().getDeviceType().equals(DeviceType.MOBILE) &&
                deviceType == null && StringUtils.isNotBlank(siteConfig.getOption("mobile_home_index")) &&
                "true".equals(siteConfig.getOption("mobile_home_redirect"))) {
            deviceType = new Cookie("deviceType", "mobile");
            deviceType.setPath("/");
            deviceType.setHttpOnly(true);
            response.addCookie(deviceType);
            response.sendRedirect(siteConfig.getOption("mobile_home_index"));
            log.info("SiteInterceptor redirect to mobile index...");
            return false;
        }

        if (!Utils.isFilePath(requestUri) &&
                userAgent.getOperatingSystem().getDeviceType().equals(DeviceType.MOBILE) &&
                deviceType != null && "mobile".equals(deviceType.getValue()) &&
                StringUtils.isNotBlank(siteConfig.getOption("mobile_home_index")) &&
                "true".equals(siteConfig.getOption("mobile_home_redirect"))) {
            log.info("SiteInterceptor redirect to mobile index");
            response.sendRedirect(siteConfig.getOption("mobile_home_index"));
            return false;
        }
        return true;
    }
}
