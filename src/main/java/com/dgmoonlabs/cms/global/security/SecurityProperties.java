package com.dgmoonlabs.cms.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("cms.security")
public record SecurityProperties(
        @DefaultValue("5") int maxLoginTryCount,
        @DefaultValue("admin") String initialAdminUsername,
        String initialAdminPassword
) {
}
