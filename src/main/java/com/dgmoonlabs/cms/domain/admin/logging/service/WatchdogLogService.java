package com.dgmoonlabs.cms.domain.admin.logging.service;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import com.dgmoonlabs.cms.domain.admin.logging.repository.WatchdogLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WatchdogLogService {
    private final WatchdogLogRepository watchdogLogRepository;

    public void save(WatchdogLog watchdogLog) {
        watchdogLogRepository.save(watchdogLog);
    }
}
