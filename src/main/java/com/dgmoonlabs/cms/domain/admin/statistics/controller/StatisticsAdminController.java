package com.dgmoonlabs.cms.domain.admin.statistics.controller;

import com.dgmoonlabs.cms.domain.admin.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StatisticsAdminController {
    private final StatisticsService statisticsService;
}
