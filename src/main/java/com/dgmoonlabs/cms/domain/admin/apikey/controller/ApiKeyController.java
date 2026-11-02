package com.dgmoonlabs.cms.domain.admin.apikey.controller;

import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeyOption;
import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeyResponse;
import com.dgmoonlabs.cms.domain.admin.apikey.dto.ApiKeyDtos.ApiKeySaveRequest;
import com.dgmoonlabs.cms.domain.admin.apikey.service.ApiKeyService;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @RequiresReauthentication
    @GetMapping
    public ResponseEntity<PagedModel<ApiKeyResponse>> search(@RequestParam(required = false) final String keyword,
                                                             @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(apiKeyService.search(keyword, pageable)));
    }

    @GetMapping("/options")
    public ResponseEntity<List<ApiKeyOption>> options() {
        return ResponseEntity.ok(apiKeyService.options());
    }

    @RequiresReauthentication
    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(apiKeyService.get(id));
    }

    @RequiresReauthentication
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid final ApiKeySaveRequest request) {
        return ResponseEntity.created(URI.create("/api/admin/api-keys/" + apiKeyService.create(request))).build();
    }

    @RequiresReauthentication
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final ApiKeySaveRequest request) {
        apiKeyService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @RequiresReauthentication
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        apiKeyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
