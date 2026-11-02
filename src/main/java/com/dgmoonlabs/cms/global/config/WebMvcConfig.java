package com.dgmoonlabs.cms.global.config;

import com.dgmoonlabs.cms.domain.admin.logging.AdminAuditInterceptor;
import com.dgmoonlabs.cms.domain.admin.site.SiteFeatureInterceptor;
import com.dgmoonlabs.cms.global.security.ReauthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final LocaleChangeInterceptor localeChangeInterceptor;
    private final AdminAuditInterceptor adminAuditInterceptor;
    private final SiteFeatureInterceptor siteFeatureInterceptor;
    private final ReauthenticationInterceptor reauthenticationInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
        registry.addInterceptor(adminAuditInterceptor).addPathPatterns("/api/admin/**");
        registry.addInterceptor(siteFeatureInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(reauthenticationInterceptor).addPathPatterns("/api/admin/**");
    }
}
