package egovframework.com.cms.support.configuration;

import egovframework.com.cms.support.interceptor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@RequiredArgsConstructor
public class MvcConfiguration implements WebMvcConfigurer {
    private final SiteInterceptor siteInterceptor;
    private final SSLInterceptor sslInterceptor;
    private final SecurityInterceptor securityInterceptor;
    private final DesignInterceptor designInterceptor;
    private final UserInterceptor userInterceptor;
    private final BoardInterceptor boardInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final StatisticsInterceptor statisticsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(siteInterceptor).addPathPatterns("/site/**");
        registry.addInterceptor(sslInterceptor).addPathPatterns("/site/**");
        registry.addInterceptor(securityInterceptor).addPathPatterns("/site/**");
        registry.addInterceptor(designInterceptor).addPathPatterns("/site/**");
        registry.addInterceptor(localeChangeInterceptor()).addPathPatterns("/site/**");
        registry.addInterceptor(userInterceptor).addPathPatterns("/site/**");
        registry.addInterceptor(boardInterceptor).addPathPatterns("/site/**/board/**");
        registry.addInterceptor(adminInterceptor).addPathPatterns("/site/**/admin/**/*.do");
        registry.addInterceptor(securityInterceptor).addPathPatterns("/site/**");
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new LocaleChangeInterceptor();
    }


}
