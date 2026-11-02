package com.dgmoonlabs.cms.domain.admin.logging.service;

import com.dgmoonlabs.cms.domain.admin.logging.AdminAuditInterceptor.AdminAuditEvent;
import com.dgmoonlabs.cms.domain.admin.logging.dto.WatchdogLogResponse;
import com.dgmoonlabs.cms.domain.admin.logging.entity.WatchdogLog;
import com.dgmoonlabs.cms.domain.admin.logging.repository.WatchdogLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchdogLogService {
    private final WatchdogLogRepository watchdogLogRepository;

    // a separate transaction keeps the audit row even when the audited request rolled back
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(final AdminAuditEvent event) {
        watchdogLogRepository.save(WatchdogLog.builder()
                .memberUsername(event.username())
                .uri(event.uri())
                .methodName(event.handler())
                .content(event.content())
                .ipAddress(event.ipAddress())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<WatchdogLogResponse> search(final String keyword, final Pageable pageable) {
        String term = keyword == null ? "" : keyword;
        return watchdogLogRepository
                .findAllByMemberUsernameContainingIgnoreCaseOrUriContainingIgnoreCaseOrderByIdDesc(term, term, pageable)
                .map(WatchdogLogResponse::from);
    }
}
