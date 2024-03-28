package com.dgmoonlabs.cms.domain.admin.menu.repository;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<WatchdogLog, Long> {
}
