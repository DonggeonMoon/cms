package com.dgmoonlabs.cms.global.security.password;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordHashSettingRepository extends JpaRepository<PasswordHashSetting, Long> {
    Optional<PasswordHashSetting> findFirstByOrderByIdAsc();
}
