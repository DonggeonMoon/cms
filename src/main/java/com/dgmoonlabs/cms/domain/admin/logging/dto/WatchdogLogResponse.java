package com.dgmoonlabs.cms.domain.admin.logging.dto;

import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;

import java.time.LocalDateTime;

public record WatchdogLogResponse(Long id, String username, String uri, String handler, String content,
                                  String ipAddress, LocalDateTime createdAt) {
    public static WatchdogLogResponse from(final WatchdogLog log) {
        return new WatchdogLogResponse(log.getId(), log.getMemberUsername(), log.getUri(), log.getMethodName(),
                log.getContent(), log.getIpAddress(), log.getCreatedAt());
    }
}
