package com.dgmoonlabs.cms.domain.common.i18n.controller;

import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.*;
import com.dgmoonlabs.cms.domain.common.i18n.service.I18nService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
public class I18nController {
    private final I18nService i18nService;

    @GetMapping("/api/i18n")
    public ResponseEntity<I18nSettingResponse> setting() {
        return ResponseEntity.ok(i18nService.getSetting());
    }

    @GetMapping("/api/i18n/messages/{language}")
    public ResponseEntity<Map<String, String>> messages(@PathVariable @Pattern(regexp = I18nDtos.LANGUAGE_CODE) final String language) {
        return ResponseEntity.ok(i18nService.getMessages(language));
    }

    @PutMapping("/api/admin/i18n")
    public ResponseEntity<I18nSettingResponse> updateSetting(@RequestBody @Valid final I18nSettingRequest request) {
        return ResponseEntity.ok(i18nService.updateSetting(request));
    }

    @GetMapping("/api/admin/i18n/bundles/{language}")
    public ResponseEntity<BundleResponse> bundle(@PathVariable @Pattern(regexp = I18nDtos.LANGUAGE_CODE) final String language,
                                                 @RequestParam(defaultValue = "PROPERTIES") final BundleFormat format) {
        return ResponseEntity.ok(i18nService.export(language, format));
    }

    @PutMapping("/api/admin/i18n/bundles/{language}")
    public ResponseEntity<BundleResponse> replace(@PathVariable @Pattern(regexp = I18nDtos.LANGUAGE_CODE) final String language,
                                                  @RequestBody @Valid final BundleRequest request) {
        return ResponseEntity.ok(i18nService.replace(language, request));
    }
}
