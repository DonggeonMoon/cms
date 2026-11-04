package com.dgmoonlabs.cms.domain.common.i18n.repository;

import com.dgmoonlabs.cms.domain.common.i18n.entity.I18nSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface I18nSettingRepository extends JpaRepository<I18nSetting, Long> {
    Optional<I18nSetting> findFirstByOrderByIdAsc();
}
