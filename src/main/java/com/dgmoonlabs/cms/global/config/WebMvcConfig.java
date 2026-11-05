package com.dgmoonlabs.cms.global.config;

import com.dgmoonlabs.cms.domain.admin.account.service.AdminAreaInterceptor;
import com.dgmoonlabs.cms.domain.admin.ipaccess.service.AdminIpInterceptor;
import com.dgmoonlabs.cms.global.web.SeoIndexRenderer;
import com.dgmoonlabs.cms.domain.admin.logging.AdminAuditInterceptor;
import com.dgmoonlabs.cms.domain.admin.site.SiteFeatureInterceptor;
import com.dgmoonlabs.cms.global.security.ReauthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final LocaleChangeInterceptor localeChangeInterceptor;
    private final AdminAuditInterceptor adminAuditInterceptor;
    private final SiteFeatureInterceptor siteFeatureInterceptor;
    private final ReauthenticationInterceptor reauthenticationInterceptor;
    private final AdminAreaInterceptor adminAreaInterceptor;
    private final AdminIpInterceptor adminIpInterceptor;
    private final SeoIndexRenderer seoIndexRenderer;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
        registry.addInterceptor(adminIpInterceptor).addPathPatterns("/api/admin/**");
        registry.addInterceptor(adminAuditInterceptor).addPathPatterns("/api/admin/**");
        registry.addInterceptor(siteFeatureInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(adminAreaInterceptor).addPathPatterns("/api/admin/**");
        registry.addInterceptor(reauthenticationInterceptor).addPathPatterns("/api/admin/**");
    }

    // 파일이 없는 클라이언트 라우트는 index.html 로 보낸다(API 경로 제외).
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(final String resourcePath, final Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        if (resourcePath.startsWith("api/") || resourcePath.contains(".")) {
                            return null;
                        }
                        return seoIndexRenderer.render(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString())
                                .map(html -> (Resource) new ByteArrayResource(html.getBytes(StandardCharsets.UTF_8)) {
                                    @Override
                                    public String getFilename() {
                                        return "index.html";
                                    }

                                    @Override
                                    public long lastModified() {
                                        return System.currentTimeMillis();
                                    }
                                })
                                .orElse(null);
                    }
                });
    }
}
