package com.dgmoonlabs.cms.domain.common.identity.repository;

import com.dgmoonlabs.cms.domain.common.identity.entity.IdentityVerificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdentityVerificationSettingRepository extends JpaRepository<IdentityVerificationSetting, Long> {
    Optional<IdentityVerificationSetting> findFirstByOrderByIdAsc();
}
