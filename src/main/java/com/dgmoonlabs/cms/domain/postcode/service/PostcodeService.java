package com.dgmoonlabs.cms.domain.postcode.service;

import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyService;
import com.dgmoonlabs.cms.domain.postcode.client.JusoAddressClient;
import com.dgmoonlabs.cms.domain.postcode.constant.PostcodeProvider;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSearchResponse;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSettingRequest;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PostcodeSettingResponse;
import com.dgmoonlabs.cms.domain.postcode.dto.PostcodeDtos.PublicPostcodeResponse;
import com.dgmoonlabs.cms.domain.postcode.entity.PostcodeSetting;
import com.dgmoonlabs.cms.domain.postcode.repository.PostcodeSettingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostcodeService {
    private static final int COUNT_PER_PAGE = 20;

    private final PostcodeSettingRepository settingRepository;
    private final ApiKeyService apiKeyService;
    private final JusoAddressClient jusoAddressClient;

    @Transactional(readOnly = true)
    public PublicPostcodeResponse getPublicSetting() {
        return new PublicPostcodeResponse(currentSetting().getProvider());
    }

    @Transactional(readOnly = true)
    public PostcodeSettingResponse getSetting() {
        return PostcodeSettingResponse.from(currentSetting());
    }

    @Transactional
    public PostcodeSettingResponse updateSetting(final PostcodeSettingRequest request) {
        if (request.provider() == PostcodeProvider.JUSO && request.jusoApiKeyId() == null) {
            throw new BusinessException(ErrorCode.POSTCODE_API_KEY_REQUIRED);
        }
        apiKeyService.requireExists(request.jusoApiKeyId());
        PostcodeSetting setting = settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(PostcodeSetting.empty()));
        setting.update(request);
        return PostcodeSettingResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public PostcodeSearchResponse search(final String keyword, final int page) {
        PostcodeSetting setting = currentSetting();
        if (setting.getProvider() != PostcodeProvider.JUSO || setting.getJusoApiKeyId() == null) {
            throw new BusinessException(ErrorCode.POSTCODE_API_KEY_REQUIRED);
        }
        return jusoAddressClient.search(apiKeyService.valueOf(setting.getJusoApiKeyId()), keyword, page, COUNT_PER_PAGE);
    }

    private PostcodeSetting currentSetting() {
        return settingRepository.findFirstByOrderByIdAsc().orElseGet(PostcodeSetting::empty);
    }
}
