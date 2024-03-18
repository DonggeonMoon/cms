package com.dgmoonlabs.cms.domain.admin.logging.repository;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchdogLogRepository extends JpaRepository<WatchdogLog, Long> {
}
