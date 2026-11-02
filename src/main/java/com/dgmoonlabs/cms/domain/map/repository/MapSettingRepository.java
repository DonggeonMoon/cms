package com.dgmoonlabs.cms.domain.map.repository;

import com.dgmoonlabs.cms.domain.map.entity.MapSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MapSettingRepository extends JpaRepository<MapSetting, Long> {
    Optional<MapSetting> findFirstByOrderByIdAsc();
}
