package com.dgmoonlabs.cms.domain.common.identity.controller;

import com.dgmoonlabs.cms.domain.common.identity.dto.IdentityVerificationDtos.*;
import com.dgmoonlabs.cms.domain.common.identity.service.IdentityVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class IdentityVerificationController {
    private final IdentityVerificationService identityVerificationService;

    @GetMapping("/api/identity-verification")
    public ResponseEntity<SettingsResponse> settings() {
        return ResponseEntity.ok(new SettingsResponse(identityVerificationService.isEnabled()));
    }

    @PostMapping("/api/identity-verifications/start")
    public ResponseEntity<VerificationSession> start(@RequestBody @Valid final StartRequest request) {
        return ResponseEntity.ok(identityVerificationService.start(request.returnUrl()));
    }

    @PostMapping("/api/identity-verifications/complete")
    public ResponseEntity<VerificationResult> complete(@RequestBody @Valid final CompleteRequest request) {
        return ResponseEntity.ok(identityVerificationService.complete(request.token()));
    }

    @PutMapping("/api/admin/identity-verification")
    public ResponseEntity<SettingsResponse> update(@RequestBody @Valid final EnabledRequest request) {
        return ResponseEntity.ok(new SettingsResponse(identityVerificationService.changeEnabled(request.enabled())));
    }
}
