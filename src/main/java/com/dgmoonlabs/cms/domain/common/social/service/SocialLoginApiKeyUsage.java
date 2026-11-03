package com.dgmoonlabs.cms.domain.common.social.service;

import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyUsage;
import com.dgmoonlabs.cms.domain.common.social.repository.SocialLoginSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialLoginApiKeyUsage implements ApiKeyUsage {
    private final SocialLoginSettingRepository repository;

    @Override
    public boolean isUsing(final Long apiKeyId) {
        return repository.findAll().stream().anyMatch(setting -> setting.uses(apiKeyId));
    }
}
