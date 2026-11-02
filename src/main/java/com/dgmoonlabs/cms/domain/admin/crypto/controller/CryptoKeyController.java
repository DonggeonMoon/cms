package com.dgmoonlabs.cms.domain.admin.crypto.controller;

import com.dgmoonlabs.cms.domain.admin.crypto.dto.CryptoKeyDtos.CryptoAlgorithmRequest;
import com.dgmoonlabs.cms.domain.admin.crypto.dto.CryptoKeyDtos.CryptoKeyResponse;
import com.dgmoonlabs.cms.domain.admin.crypto.dto.CryptoKeyDtos.ReencryptResponse;
import com.dgmoonlabs.cms.domain.admin.crypto.service.CryptoKeyService;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/crypto-key")
@RequiredArgsConstructor
@RequiresReauthentication
public class CryptoKeyController {
    private final CryptoKeyService cryptoKeyService;

    @GetMapping
    public ResponseEntity<CryptoKeyResponse> get() {
        return ResponseEntity.ok(cryptoKeyService.get());
    }

    @PostMapping("/regenerate")
    public ResponseEntity<ReencryptResponse> regenerate() {
        return ResponseEntity.ok(cryptoKeyService.regenerate());
    }

    @PutMapping("/algorithm")
    public ResponseEntity<ReencryptResponse> changeAlgorithm(@RequestBody @Valid final CryptoAlgorithmRequest request) {
        return ResponseEntity.ok(cryptoKeyService.changeAlgorithm(request.algorithm()));
    }
}
