package com.dgmoonlabs.cms.domain.admin.security.service;

import com.dgmoonlabs.cms.domain.admin.security.dto.LoginSecurityDtos.LoginSecurityResponse;
import com.dgmoonlabs.cms.domain.admin.security.entity.LoginSecuritySetting;
import com.dgmoonlabs.cms.domain.admin.security.repository.LoginSecuritySettingRepository;
import com.dgmoonlabs.cms.global.security.SecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 실패 허용 횟수를 관리자가 화면에서 바꿀 수 있게 DB에 둔다. 값이 없으면 application.yml 기본값을 쓴다. */
@Service
@RequiredArgsConstructor
public class LoginSecuritySettingService {
    private final LoginSecuritySettingRepository settingRepository;
    private final SecurityProperties securityProperties;

    @Transactional(readOnly = true)
    public int maxLoginTryCount() {
        return settingRepository.findFirstByOrderByIdAsc()
                .map(LoginSecuritySetting::getMaxLoginTryCount)
                .orElseGet(securityProperties::maxLoginTryCount);
    }

    @Transactional(readOnly = true)
    public LoginSecurityResponse getSettings() {
        return new LoginSecurityResponse(maxLoginTryCount());
    }

    @Transactional
    public LoginSecurityResponse update(final int maxLoginTryCount) {
        settingRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> settingRepository.save(LoginSecuritySetting.builder()
                        .maxLoginTryCount(securityProperties.maxLoginTryCount()).build()))
                .changeMaxLoginTryCount(maxLoginTryCount);
        return new LoginSecurityResponse(maxLoginTryCount);
    }
}
