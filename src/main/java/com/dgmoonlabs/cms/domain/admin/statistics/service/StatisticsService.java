package com.dgmoonlabs.cms.domain.admin.statistics.service;

import com.dgmoonlabs.cms.domain.admin.statistics.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;
}
