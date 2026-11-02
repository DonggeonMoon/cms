package com.dgmoonlabs.cms.domain.admin.config.controller;

import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigResponse;
import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigSaveRequest;
import com.dgmoonlabs.cms.domain.admin.config.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ConfigController {
    private final ConfigService configService;

    @GetMapping("/api/configs")
    public ResponseEntity<Map<String, String>> publicConfigs() {
        return ResponseEntity.ok(configService.getPublicConfigs());
    }

    @GetMapping("/api/admin/configs")
    public ResponseEntity<PagedModel<ConfigResponse>> search(@RequestParam(required = false) final String keyword,
                                                             @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(configService.searchConfigs(keyword, pageable)));
    }

    @GetMapping("/api/admin/configs/{id}")
    public ResponseEntity<ConfigResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(configService.getConfig(id));
    }

    @PostMapping("/api/admin/configs")
    public ResponseEntity<Void> create(@RequestBody @Valid final ConfigSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/configs/" + configService.createConfig(request))).build();
    }

    @PutMapping("/api/admin/configs/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final ConfigSaveRequest request) {
        configService.updateConfig(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/configs/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }
}
