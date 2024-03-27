package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsLogRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsLogService {
    private final StatisticsLogRepository statisticsLogRepository;

    @Transactional
    public void saveStatisticsLog(StatisticsLogRequest request) {
        statisticsLogRepository.save(request.toEntity());
    }

    @Transactional(readOnly = true)
    public Page<StatisticsLog> getStatisticsLogs(StatisticsLogRequest statisticsLogRequest, Pageable pageable) {
        return statisticsLogRepository.find(statisticsLogRequest, pageable);
    }

    @Transactional(readOnly = true)
    public Page<StatisticsLog> getStatisticsLogsWithoutPaging(StatisticsLogRequest statisticsLogRequest, Pageable pageable) {
        return statisticsLogRepository.find(statisticsLogRequest, pageable);
    }

    @Transactional
    public StatisticsLog getStatisticsLog(long id) {
        return statisticsLogRepository.findById(id).orElseThrow(RuntimeException::new);
    }
}
