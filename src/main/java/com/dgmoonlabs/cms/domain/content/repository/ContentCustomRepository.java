package com.dgmoonlabs.cms.domain.content.repository;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.Statistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentCustomRepository {
    Page<Statistics> find(StatisticsRequest statisticsRequest, Pageable pageable);

    List<Statistics> find(StatisticsRequest statisticsRequest);
}
