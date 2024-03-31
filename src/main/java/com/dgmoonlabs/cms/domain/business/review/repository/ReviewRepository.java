package com.dgmoonlabs.cms.domain.business.review.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Statistics, Long>, StatisticsCustomRepository {
}
