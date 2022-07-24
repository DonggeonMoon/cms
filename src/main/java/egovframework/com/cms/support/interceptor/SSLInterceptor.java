package egovframework.com.cms.support.interceptor;

import egovframework.com.cms.config.service.ConfigOptionService;
import egovframework.com.cms.support.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class SSLInterceptor implements HandlerInterceptor {
    private final ConfigOptionService configOptionService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        String requestUri = Utils.getFixedRequestUri(request);

        if (Utils.isDevPath(request.getServerName())) {
            return true;
        }

        if (!"true".equalsIgnoreCase(configOptionService.getNoCacheOption("security", "ssl").getOptValue())) {
            log.info("SSLInterceptor SSL is false in CONFIG_OPTION table.");
            return true;
        }

        log.info("SSLInterceptor SSL is true in CONFIG_OPTION table");

        String currentScheme = request.getScheme();
        String sslPatterns = configOptionService.getNoCacheOption("security", "ssl_pattern").getOptValue();
        String sslPort = configOptionService.getNoCacheOption("security", "ssl_port").getOptValue();

        if (StringUtils.isBlank(sslPatterns) && !"https".equalsIgnoreCase(currentScheme)) {
            log.info("SSLInterceptor SSL is applying to all urls. redirect to HTTPS");
            response.sendRedirect(Utils.getSchemeDomainPort(request, "https", sslPort) + Utils.getRequestUriWithParameters(request));
            return true;
        }

        if (StringUtils.isBlank(sslPatterns)) {
            return true;
        }

        String[] patterns = sslPatterns.split("\n");
        boolean isPatternedSSL = false;
        for (String pattern : patterns) {
            pattern = pattern.trim();
            if (StringUtils.isBlank(pattern)) {
                continue;
            }

            if (!"https".equalsIgnoreCase(currentScheme)
                    && requestUri.matches(pattern)) {
                log.info("SSLInterceptor SSL pattern[{}] matched, redirect to HTTPS.", pattern);
                response.sendRedirect(Utils.getSchemeDomainPort(request, "https", sslPort));
                return false;
            }

            if ("https".equalsIgnoreCase(currentScheme)
                    && requestUri.matches(pattern)) {
                isPatternedSSL = true;
                break;
            }
        }

        if ("https".equalsIgnoreCase(currentScheme)
                && StringUtils.isNotBlank(sslPatterns)
                && "true".equalsIgnoreCase(configOptionService.getNoCacheOption("security", "ssl_force_http").getOptValue())
                && !isPatternedSSL) {
            response.sendRedirect(Utils.getSchemeDomainPort(request, "http", null) + Utils.getRequestUriWithParameters(request));
            return false;
        }
        return true;
    }
}
