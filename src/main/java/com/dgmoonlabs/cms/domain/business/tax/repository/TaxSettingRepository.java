package com.dgmoonlabs.cms.domain.business.tax.repository;

import com.dgmoonlabs.cms.domain.business.tax.entity.TaxSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaxSettingRepository extends JpaRepository<TaxSetting, Long> {
    Optional<TaxSetting> findFirstByOrderByIdAsc();
}
