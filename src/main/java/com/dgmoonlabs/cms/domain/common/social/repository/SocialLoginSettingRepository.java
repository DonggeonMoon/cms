package com.dgmoonlabs.cms.domain.common.social.repository;

import com.dgmoonlabs.cms.domain.common.social.constant.SocialProvider;
import com.dgmoonlabs.cms.domain.common.social.entity.SocialLoginSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialLoginSettingRepository extends JpaRepository<SocialLoginSetting, Long> {
    Optional<SocialLoginSetting> findByProvider(SocialProvider provider);
}
