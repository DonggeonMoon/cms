package com.dgmoonlabs.cms.domain.admin.ipaccess.repository;

import com.dgmoonlabs.cms.domain.admin.ipaccess.entity.AdminIpSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminIpSettingRepository extends JpaRepository<AdminIpSetting, Long> {
    Optional<AdminIpSetting> findFirstByOrderByIdAsc();
}
