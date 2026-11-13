package com.dgmoonlabs.cms.domain.admin.security.repository;

import com.dgmoonlabs.cms.domain.admin.security.entity.LoginSecuritySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginSecuritySettingRepository extends JpaRepository<LoginSecuritySetting, Long> {
    Optional<LoginSecuritySetting> findFirstByOrderByIdAsc();
}
