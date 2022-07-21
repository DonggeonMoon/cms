package egovframework.com.cms.support.interceptor;

import egovframework.com.cms.menu.model.Menu;
import egovframework.com.cms.site.model.Site;
import egovframework.com.cms.support.Constant;
import egovframework.com.cms.support.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DesignInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = Utils.getFixedRequestUri(request);
        Site currentSite = (Site) request.getAttribute("currentSite");

        if (!requestUri.startsWith(Utils.getAdminPath(currentSite)) && requestUri.endsWith(".do")) {
            log.warn("DesignInterceptor#preHandle - request URI is not valid. 404 returned.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }

        request.setAttribute("WAS_PATH", Constant.WAS_PATH);
        request.setAttribute("ADMIN_PATH", Utils.getAdminPath(currentSite));
        request.setAttribute("APP_PATH", Utils.getAppPath(currentSite));
        request.setAttribute("API_PATH", Constant.API_PATH);
        request.setAttribute("UPLOAD_PATH", Constant.UPLOAD_PATH);
        request.setAttribute("SITE_UPLOAD_PATH", Constant.UPLOAD_PATH + "/" + currentSite.getSiteId());

        if (Utils.isUserPath(requestUri) && StringUtils.isNotBlank(request.getParameter("themePreview"))) {
            request.setAttribute("theme", request.getParameter("themePreview"));
        } else {
            request.setAttribute("theme", currentSite.getSiteTheme());
        }

        Menu currentMenu;


        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        String requestUri = Utils.getFixedRequestUri(request);
        Site currentSite = (Site) request.getAttribute("currentSite");
        Map<String, String> design = new HashMap<>();

        design.put("asset", Constant.WAS_PATH + "/design/asset");
        design.put("common", Constant.WAS_PATH + "/design/common");

        if (isDesignApplyUri(requestUri, currentSite)) {
            String theme = (String) request.getAttribute("theme");

            StringBuilder themeDir = new StringBuilder(50);
            themeDir.append(Constant.THEME_ROOT);
            design.put("themeRoot", Constant.THEME_ROOT);
            themeDir.append(theme);
            design.put("themeDir", themeDir.toString());


        }

    }

    private boolean isDesignApplyUri(String requestUri, Site currentSite) {
        return Utils.isUserPath(requestUri)
                && !Utils.isFilePath(requestUri)
                && !Utils.isFeedPath(requestUri)
                && !Utils.isApiPath(requestUri);
    }
}
