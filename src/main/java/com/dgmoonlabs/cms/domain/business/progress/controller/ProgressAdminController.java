package com.dgmoonlabs.cms.domain.business.progress.controller;

import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.business.progress.dto.ProgressDtos.*;
import com.dgmoonlabs.cms.domain.business.progress.service.ProgressStageService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/progress")
@RequiredArgsConstructor
public class ProgressAdminController {
    private final ProgressStageService progressStageService;

    @GetMapping("/{targetType}/templates")
    public ResponseEntity<List<StageTemplateResponse>> templates(@PathVariable final ProgressTargetType targetType) {
        return ResponseEntity.ok(progressStageService.listTemplates(targetType));
    }

    @PostMapping("/{targetType}/templates")
    public ResponseEntity<StageTemplateResponse> createTemplate(@PathVariable final ProgressTargetType targetType,
                                                                 @RequestBody @Valid final StageTemplateSaveRequest request) {
        return ResponseEntity.ok(progressStageService.createTemplate(targetType, request));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<StageTemplateResponse> updateTemplate(@PathVariable final Long id,
                                                                 @RequestBody @Valid final StageTemplateSaveRequest request) {
        return ResponseEntity.ok(progressStageService.updateTemplate(id, request));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable final Long id) {
        progressStageService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<ProgressResponse> get(@PathVariable final ProgressTargetType targetType, @PathVariable final Long targetId) {
        return ResponseEntity.ok(progressStageService.get(targetType, targetId));
    }

    @PostMapping("/{targetType}/{targetId}/advance")
    public ResponseEntity<StageEventResponse> advance(@AuthenticationPrincipal final LoginUser admin,
                                                       @PathVariable final ProgressTargetType targetType,
                                                       @PathVariable final Long targetId,
                                                       @RequestBody @Valid final AdvanceRequest request) {
        return ResponseEntity.ok(progressStageService.advance(targetType, targetId, request, admin.name()));
    }
}
