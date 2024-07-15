package com.dgmoonlabs.cms.domain.admin.config.controller;

import com.dgmoonlabs.cms.domain.admin.config.dto.ConfigRequest;
import com.dgmoonlabs.cms.domain.admin.config.entity.Config;
import com.dgmoonlabs.cms.domain.admin.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/config")
public class ConfigRestController {
    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<Page<Config>> getMultiple(Pageable pageable) {
        return ResponseEntity.ok().body(configService.getMultiple(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Config> getOne(@PathVariable Long id) {
        return ResponseEntity.ok().body(configService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<Page<Config>> save(@RequestParam ConfigRequest request) {
        return ResponseEntity.created(
                UriComponentsBuilder.fromUriString("/admin/config")
                        .queryParam("id", configService.save(request))
                        .build().toUri()
        ).build();
    }

    @DeleteMapping
    public ResponseEntity<Long> delete(@RequestParam Long id) {
        configService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
