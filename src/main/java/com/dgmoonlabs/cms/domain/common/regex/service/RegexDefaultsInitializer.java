package com.dgmoonlabs.cms.domain.common.regex.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class RegexDefaultsInitializer implements ApplicationRunner {
    private final RegexService regexService;

    @Override
    public void run(final ApplicationArguments args) {
        regexService.registerDefaults();
    }
}
