package com.dgmoonlabs.cms.domain.common.social.service;

import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyService;
import com.dgmoonlabs.cms.domain.common.social.client.SocialCredentials;
import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.domain.common.social.dto.SocialDtos.ProviderOption;
import com.dgmoonlabs.cms.domain.common.social.dto.SocialDtos.SocialLoginSettingRequest;
import com.dgmoonlabs.cms.domain.common.social.dto.SocialDtos.SocialLoginSettingResponse;
import com.dgmoonlabs.cms.domain.common.social.entity.SocialLoginSetting;
import com.dgmoonlabs.cms.domain.common.social.repository.SocialLoginSettingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialLoginSettingService {
    private final SocialLoginSettingRepository repository;
    private final ApiKeyService apiKeyService;
    private final PublicBaseUrl publicBaseUrl;

    @Transactional(readOnly = true)
    public List<SocialLoginSettingResponse> getSettings() {
        return Arrays.stream(SocialProvider.values())
                .map(provider -> SocialLoginSettingResponse.of(find(provider), redirectUri(provider)))
                .toList();
    }

    @Transactional
    public SocialLoginSettingResponse update(final SocialProvider provider, final SocialLoginSettingRequest request) {
        apiKeyService.requireExists(request.clientIdKeyId());
        apiKeyService.requireExists(request.clientSecretKeyId());
        apiKeyService.requireExists(request.applePrivateKeyKeyId());
        SocialLoginSetting setting = repository.findByProvider(provider)
                .orElseGet(() -> repository.save(SocialLoginSetting.empty(provider)));
        setting.update(request.enabled(), request.clientIdKeyId(), request.clientSecretKeyId(),
                trim(request.appleTeamId()), trim(request.appleKeyId()), request.applePrivateKeyKeyId());
        if (setting.isEnabled() && !setting.isConfigured()) {
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_NOT_CONFIGURED);
        }
        return SocialLoginSettingResponse.of(setting, redirectUri(provider));
    }

    @Transactional(readOnly = true)
    public List<ProviderOption> getEnabledProviders() {
        return Arrays.stream(SocialProvider.values())
                .filter(provider -> find(provider).isEnabled())
                .map(ProviderOption::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public SocialCredentials credentials(final SocialProvider provider) {
        SocialLoginSetting setting = find(provider);
        if (!setting.isEnabled() || !setting.isConfigured()) {
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_DISABLED);
        }
        return new SocialCredentials(apiKeyService.valueOf(setting.getClientIdKeyId()),
                apiKeyService.valueOf(setting.getClientSecretKeyId()), setting.getAppleTeamId(), setting.getAppleKeyId(),
                apiKeyService.valueOf(setting.getApplePrivateKeyKeyId()));
    }

    public String redirectUri(final SocialProvider provider) {
        return publicBaseUrl.get() + "/api/auth/oauth/" + provider.path() + "/callback";
    }

    private SocialLoginSetting find(final SocialProvider provider) {
        return repository.findByProvider(provider).orElseGet(() -> SocialLoginSetting.empty(provider));
    }

    private static String trim(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
