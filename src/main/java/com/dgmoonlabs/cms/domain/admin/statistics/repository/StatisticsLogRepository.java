package com.dgmoonlabs.cms.domain.admin.statistics.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsLogRepository extends JpaRepository<StatisticsLog, Long> {
    Page<StatisticsLog> findAllByOrderByIdDesc(Pageable pageable);
}
