package com.dgmoonlabs.cms.domain.admin.statistics.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsRepository extends JpaRepository<StatisticsLog, Long> {
}
