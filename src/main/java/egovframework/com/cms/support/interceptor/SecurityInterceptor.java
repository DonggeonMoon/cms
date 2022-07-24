package egovframework.com.cms.support.interceptor;

import egovframework.com.cms.config.dto.Config;
import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.member.model.Member;
import egovframework.com.cms.member.service.MemberService;
import egovframework.com.cms.security.service.SecurityService;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.taglib.Functions;
import egovframework.com.cms.support.util.CookieSSO;
import egovframework.com.cms.support.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityInterceptor implements HandlerInterceptor {
    private static final String ONCE_PER_REQUEST_ATTRIBUTE_NAME = SecurityInterceptor.class.getName() + ".ATTR";

    private final SecurityService securityService;
    private final ConfigOptionService configOptionService;
    private final MemberService memberService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String requestUri = Utils.getFixedRequestUri(request);
        Site currentSite = (Site) request.getAttribute("currentSite");
        Member currentUser = (Member) request.getSession().getAttribute("currentUser");
        Config globalConfig = configOptionService.getConfig("global");
        String cookieDomain = globalConfig.getOption("cookie_domain");

        if (!"true".equals(globalConfig.getOption("use_cookie_sso"))) {
            log.warn("SecurityInterceptor COOKIE DOMAIN CONFIG OPTION VALUE IS MISSING!!");
        }

        if ("true".equals(globalConfig.getOption("use_cookie_sso"))
                && StringUtils.isNotBlank(cookieDomain)
                && CookieSSO.getSSOCookie(request, "cms_", cookieDomain) == null
        ) {
            log.info("SecurityInterceptor NO SSO Cookie");
        }

        if ("true".equals(globalConfig.getOption("use_cookie_sso"))
                && StringUtils.isNotBlank(cookieDomain)
                && CookieSSO.getSSOCookie(request, "cms_", cookieDomain) == null
                && request.getAttribute("loginId") != null
        ) {
            request.getSession().invalidate();
            currentUser = null;
            log.info("SecurityInterceptor Killing Session..");
            Utils.deleteCookie(request, response, Constant.SECURITY_ASYNC_COOKIE_NAME);
        }

        String ssoMemberId = CookieSSO.getUserId(request, response, "cms_", cookieDomain);
        currentUser = memberService.getMember(ssoMemberId);
        if ("true".equals(globalConfig.getOption("use_cookie_sso"))
                && StringUtils.isNotBlank(cookieDomain)
                && CookieSSO.getSSOCookie(request, "cms_", cookieDomain) != null
                && currentUser == null && currentUser.isGuest() || request.getSession().getAttribute("loginId") == null
                && currentUser != null
        ) {
            request.getSession().setAttribute("currentUser", currentUser);
            request.getSession().setAttribute("loginId", currentUser.getMemberId());
            log.info("SecurityInterceptor SSO Cookie was decrypted successfully!! Setting SSO member information to session");
            Utils.deleteCookie(request, response, Constant.SECURITY_ASYNC_COOKIE_NAME);
        }

        if ("true".equals(globalConfig.getOption("use_cookie_sso"))
                && StringUtils.isNotBlank(cookieDomain)
                && CookieSSO.getSSOCookie(request, "cms_", cookieDomain) != null
                && currentUser == null && currentUser.isGuest() || request.getSession().getAttribute("loginId") == null
                && currentUser == null
        ) {
            log.warn("SecurityInterceptor SOMETHING WRONG HAPPENED WHILE DECRYPTING SSO(DB USER IS NULL");
        }

        if ("true".equals(globalConfig.getOption("use_cookie_sso"))
                && StringUtils.isBlank(cookieDomain)
        ) {
            log.warn("SecurityInterceptor SOMETHING WRONG HAPPENED WHILE DECRYPTING SSO COOKIE(ID IS NULL)");
        }

        if (currentUser == null && request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        if (currentUser == null && requestUri.equals(Utils.getAdminPath(currentSite) + "/media/upload.do")
                || requestUri.equals(Utils.getAdminPath(currentSite) + "/media/multiMediaUpload.do")
                && "POST".equalsIgnoreCase(request.getMethod())) {
            String ffMemberid = Functions.decrypt(request.getParameter("flashToken"));
        }
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
    }

    private String createSecurityToken(String memberId, String millis, String mode) {
        StringBuilder sb = new StringBuilder(100);

        if ("form".equals(mode) || "async".equals(mode)) {
            sb.append(memberId);
            sb.append(millis);
            sb.append(Constant.SECURITY_TOKEN_SALT);
            sb.append(mode);
        }

        return DigestUtils.sha256Hex(sb.toString().trim());
    }

    private String createSecurityTokenQueryString(String token) {
        StringBuilder sb = new StringBuilder(100);
        if (StringUtils.isNotBlank(token)) {
            sb.append(Constant.SECURITY_TOKEN_NAME);
            sb.append("=");
            sb.append(token);
        }
        return sb.toString();
    }

    private String createSecurityTokenTag(String token) {
        StringBuilder sb = new StringBuilder(200);
        if (StringUtils.isNotBlank(token)) {
            sb.append("<input type=\"hidden\" name=\"");
            sb.append(Constant.SECURITY_TOKEN_NAME);
            sb.append("\" value=\"");
            sb.append(token);
            sb.append("\" />");
        }
        return sb.toString();
    }
}
