package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsLogRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsLogService {
    private final StatisticsLogRepository statisticsLogRepository;

    @Transactional
    public void saveStatisticsLog(StatisticsLogRequest request) {
        statisticsLogRepository.save(request.toEntity());
    }

    @Transactional
    public List<StatisticsLog> getStatisticsLogs() {
        return statisticsLogRepository.findAll();
    }

    @Transactional
    public void getStatisticsLog(long id) {
        statisticsLogRepository.findById(id);
    }

    @Transactional
    public void updateStatisticsLog(StatisticsLogRequest request) {
        StatisticsLog statisticsLog = statisticsLogRepository.findById(request.getId()).orElseThrow(RuntimeException::new);
        statisticsLog.update(request);
    }
}
