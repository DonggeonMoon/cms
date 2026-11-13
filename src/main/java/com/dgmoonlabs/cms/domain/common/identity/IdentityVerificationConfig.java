package com.dgmoonlabs.cms.domain.common.identity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdentityVerificationConfig {
    @Bean
    @ConditionalOnMissingBean(IdentityVerificationProvider.class)
    public IdentityVerificationProvider identityVerificationProvider() {
        return new NoopIdentityVerificationProvider();
    }
}
