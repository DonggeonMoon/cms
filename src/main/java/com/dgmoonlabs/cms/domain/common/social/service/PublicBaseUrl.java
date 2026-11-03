package com.dgmoonlabs.cms.domain.common.social.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// X-Forwarded-* 를 보내지 않는 프록시 뒤에서는 cms.public-base-url 로 주소를 지정한다.
@Component
public class PublicBaseUrl {
    private final String configured;

    public PublicBaseUrl(@Value("${cms.public-base-url:}") final String configured) {
        this.configured = configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
    }

    public String get() {
        return configured.isBlank() ? ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() : configured;
    }
}
