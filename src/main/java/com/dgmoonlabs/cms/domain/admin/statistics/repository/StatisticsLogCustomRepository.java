package com.dgmoonlabs.cms.domain.admin.statistics.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsLogRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StatisticsLogCustomRepository {
    Page<StatisticsLog> find(StatisticsLogRequest statisticsLogRequest, Pageable pageable);

    List<StatisticsLog> find(StatisticsLogRequest statisticsLogRequest);
}
