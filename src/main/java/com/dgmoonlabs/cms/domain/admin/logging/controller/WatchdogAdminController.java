package com.dgmoonlabs.cms.domain.admin.logging.controller;

import com.dgmoonlabs.cms.domain.admin.logging.dto.WatchdogLogResponse;
import com.dgmoonlabs.cms.domain.admin.logging.service.WatchdogLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WatchdogAdminController {
    private final WatchdogLogService watchdogLogService;

    @GetMapping("/api/admin/audit-logs")
    public ResponseEntity<PagedModel<WatchdogLogResponse>> search(@RequestParam(required = false) final String keyword,
                                                                  @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(watchdogLogService.search(keyword, pageable)));
    }
}
