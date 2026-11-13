package com.dgmoonlabs.cms.domain.common.identity.service;

import com.dgmoonlabs.cms.domain.common.identity.IdentityVerificationProvider;
import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.VerificationResult;
import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.VerificationSession;
import com.dgmoonlabs.cms.domain.common.identity.entity.IdentityVerificationSetting;
import com.dgmoonlabs.cms.domain.common.identity.repository.IdentityVerificationSettingRepository;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdentityVerificationService {
    private final IdentityVerificationSettingRepository settingRepository;
    private final IdentityVerificationProvider provider;

    @Transactional(readOnly = true)
    public boolean isEnabled() {
        return settingRepository.findFirstByOrderByIdAsc().map(IdentityVerificationSetting::isEnabled).orElse(false);
    }

    @Transactional
    public boolean changeEnabled(final boolean enabled) {
        settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(IdentityVerificationSetting.builder().enabled(false).build()))
                .changeEnabled(enabled);
        return enabled;
    }

    @Transactional(readOnly = true)
    public VerificationSession start(final String returnUrl) {
        requireEnabled();
        return provider.start(returnUrl);
    }

    @Transactional(readOnly = true)
    public VerificationResult complete(final String token) {
        requireEnabled();
        return provider.complete(token);
    }

    private void requireEnabled() {
        if (!isEnabled()) {
            throw new BusinessException(ErrorCode.IDENTITY_VERIFICATION_DISABLED);
        }
    }
}
