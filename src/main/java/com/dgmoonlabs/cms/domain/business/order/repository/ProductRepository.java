package com.dgmoonlabs.cms.domain.business.order.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Statistics, Long>, StatisticsCustomRepository {
}
