package com.dgmoonlabs.cms.domain.common.file.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<Statistics, Long>, StatisticsCustomRepository {
}
