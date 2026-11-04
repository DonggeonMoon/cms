package com.dgmoonlabs.cms.domain.business.store.repository;

import com.dgmoonlabs.cms.domain.business.store.entity.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreSettingRepository extends JpaRepository<StoreSetting, Long> {
    Optional<StoreSetting> findFirstByOrderByIdAsc();
}
