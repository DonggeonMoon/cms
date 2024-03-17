package com.dgmoonlabs.cms.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class I18nConfig {
    @Value("${locale.parameterName}")
    private String paramName;

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName(paramName);
        localeChangeInterceptor.setIgnoreInvalidLocale(true);
        return localeChangeInterceptor;
    }
}
