package com.dgmoonlabs.cms.domain.admin.ipaccess.controller;

import com.dgmoonlabs.cms.domain.admin.ipaccess.dto.AllowedIpDtos.AllowedIpSaveRequest;
import com.dgmoonlabs.cms.domain.admin.ipaccess.dto.AllowedIpDtos.AllowedIpSettings;
import com.dgmoonlabs.cms.domain.admin.ipaccess.dto.AllowedIpDtos.EnabledRequest;
import com.dgmoonlabs.cms.domain.admin.ipaccess.service.AdminIpAccessService;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/allowed-ips")
@RequiredArgsConstructor
public class AllowedIpController {
    private final AdminIpAccessService adminIpAccessService;

    @GetMapping
    public ResponseEntity<AllowedIpSettings> settings(final HttpServletRequest request) {
        return ResponseEntity.ok(adminIpAccessService.getSettings(request.getRemoteAddr()));
    }

    @RequiresReauthentication
    @PostMapping
    public ResponseEntity<AllowedIpSettings> create(@RequestBody @Valid final AllowedIpSaveRequest body,
                                                    final HttpServletRequest request) {
        return ResponseEntity.ok(adminIpAccessService.create(body, request.getRemoteAddr()));
    }

    @RequiresReauthentication
    @PutMapping("/{id}")
    public ResponseEntity<AllowedIpSettings> update(@PathVariable final Long id, @RequestBody @Valid final AllowedIpSaveRequest body,
                                                    final HttpServletRequest request) {
        return ResponseEntity.ok(adminIpAccessService.update(id, body, request.getRemoteAddr()));
    }

    @RequiresReauthentication
    @DeleteMapping("/{id}")
    public ResponseEntity<AllowedIpSettings> delete(@PathVariable final Long id, final HttpServletRequest request) {
        return ResponseEntity.ok(adminIpAccessService.delete(id, request.getRemoteAddr()));
    }

    @RequiresReauthentication
    @PutMapping("/enabled")
    public ResponseEntity<AllowedIpSettings> changeEnabled(@RequestBody @Valid final EnabledRequest body,
                                                           final HttpServletRequest request) {
        return ResponseEntity.ok(adminIpAccessService.changeEnabled(body.enabled(), request.getRemoteAddr()));
    }
}
