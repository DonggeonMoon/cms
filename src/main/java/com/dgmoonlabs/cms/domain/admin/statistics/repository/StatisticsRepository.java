package com.dgmoonlabs.cms.domain.admin.statistics.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
}
