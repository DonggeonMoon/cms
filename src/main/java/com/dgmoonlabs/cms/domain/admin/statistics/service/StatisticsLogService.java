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

    @Transactional
    public Page<StatisticsLog> getStatisticsLogs(Pageable pageable) {
        return statisticsLogRepository.findAll(pageable);
    }

    @Transactional
    public StatisticsLog getStatisticsLog(long id) {
        return statisticsLogRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateStatisticsLog(StatisticsLogRequest request) {
        statisticsLogRepository.findById(request.getId())
                .orElseThrow(RuntimeException::new)
                .update(request);
    }
}
