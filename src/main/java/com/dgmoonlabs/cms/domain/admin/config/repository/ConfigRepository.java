package com.dgmoonlabs.cms.domain.admin.config.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<StatisticsLog, Long>, ConfigCustomRepository {
}
