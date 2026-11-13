package com.dgmoonlabs.cms.domain.admin.security.controller;

import com.dgmoonlabs.cms.domain.admin.security.dto.LoginSecurityDtos.LoginSecurityResponse;
import com.dgmoonlabs.cms.domain.admin.security.dto.LoginSecurityDtos.LoginSecuritySaveRequest;
import com.dgmoonlabs.cms.domain.admin.security.service.LoginSecuritySettingService;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/login-security")
@RequiredArgsConstructor
public class LoginSecurityController {
    private final LoginSecuritySettingService loginSecuritySettingService;

    @GetMapping
    public ResponseEntity<LoginSecurityResponse> settings() {
        return ResponseEntity.ok(loginSecuritySettingService.getSettings());
    }

    @RequiresReauthentication
    @PutMapping
    public ResponseEntity<LoginSecurityResponse> update(@RequestBody @Valid final LoginSecuritySaveRequest request) {
        return ResponseEntity.ok(loginSecuritySettingService.update(request.maxLoginTryCount()));
    }
}
