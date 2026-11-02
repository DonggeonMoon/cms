package com.dgmoonlabs.cms.domain.map.service;

import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyUsage;
import com.dgmoonlabs.cms.domain.map.repository.MapSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MapApiKeyUsage implements ApiKeyUsage {
    private final MapSettingRepository settingRepository;

    @Override
    public boolean isUsing(final Long apiKeyId) {
        return settingRepository.findFirstByOrderByIdAsc().map(setting -> setting.uses(apiKeyId)).orElse(false);
    }
}
