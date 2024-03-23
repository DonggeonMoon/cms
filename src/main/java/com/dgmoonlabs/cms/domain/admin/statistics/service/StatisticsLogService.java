package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsLogRequest;
import com.dgmoonlabs.cms.domain.admin.statistics.entity.StatisticsLog;
import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
        return new PageImpl<>(
                statisticsLogRepository.find(statisticsLogRequest, pageable),
                pageable,
                statisticsLogRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public Page<StatisticsLog> getStatisticsLogsWithoutPaging(StatisticsLogRequest statisticsLogRequest, Pageable pageable) {
        return new PageImpl<>(
                statisticsLogRepository.find(statisticsLogRequest, pageable)
        );
    }

    @Transactional
    public StatisticsLog getStatisticsLog(long id) {
        return statisticsLogRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public void updateStatisticsLog(StatisticsLogRequest request) {
        statisticsLogRepository.findById(request.getId())
                .orElseThrow(RuntimeException::new)
                .update(
                        request.getMemberUsername(),
                        request.getUrl(),
                        request.getRequestMethod(),
                        request.getReferer(),
                        request.getIpAddress()
                );
    }
}
