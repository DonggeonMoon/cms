package com.dgmoonlabs.cms.domain.admin.logging.controller;

import com.dgmoonlabs.cms.domain.admin.logging.service.WatchdogLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WatchdogAdminController {
    private final WatchdogLogService watchdogLogService;
}
