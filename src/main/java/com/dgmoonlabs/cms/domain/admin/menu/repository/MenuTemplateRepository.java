package com.dgmoonlabs.cms.domain.admin.menu.repository;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import com.dgmoonlabs.cms.domain.admin.menu.entity.MenuTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuTemplateRepository extends JpaRepository<MenuTemplate, Long> {
}
