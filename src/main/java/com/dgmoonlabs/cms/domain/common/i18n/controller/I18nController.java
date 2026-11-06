package com.dgmoonlabs.cms.domain.common.i18n.controller;

import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.I18nSettingRequest;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.I18nSettingResponse;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.TranslationResponse;
import com.dgmoonlabs.cms.domain.common.i18n.dto.I18nDtos.TranslationSaveRequest;
import com.dgmoonlabs.cms.domain.common.i18n.service.I18nService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
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

    @GetMapping("/api/admin/i18n/translations/{language}")
    public ResponseEntity<List<TranslationResponse>> translations(
            @PathVariable @Pattern(regexp = I18nDtos.LANGUAGE_CODE) final String language) {
        return ResponseEntity.ok(i18nService.list(language));
    }

    @PostMapping("/api/admin/i18n/translations/{language}")
    public ResponseEntity<TranslationResponse> createTranslation(
            @PathVariable @Pattern(regexp = I18nDtos.LANGUAGE_CODE) final String language,
            @RequestBody @Valid final TranslationSaveRequest request) {
        TranslationResponse created = i18nService.create(language, request);
        return ResponseEntity.created(URI.create("/api/admin/i18n/translations/" + created.id())).body(created);
    }

    @PutMapping("/api/admin/i18n/translations/{id}")
    public ResponseEntity<TranslationResponse> updateTranslation(@PathVariable final Long id,
                                                                  @RequestBody @Valid final TranslationSaveRequest request) {
        return ResponseEntity.ok(i18nService.update(id, request));
    }

    @DeleteMapping("/api/admin/i18n/translations/{id}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable final Long id) {
        i18nService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
