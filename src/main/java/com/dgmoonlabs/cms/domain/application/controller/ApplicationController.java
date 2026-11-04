package com.dgmoonlabs.cms.domain.application.controller;

import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.*;
import com.dgmoonlabs.cms.domain.application.service.ApplicationService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @GetMapping("/api/applications")
    public ResponseEntity<PagedModel<FormResponse>> forms(@RequestParam(required = false) final String keyword,
                                                          @PageableDefault(size = 12) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(applicationService.getForms(keyword, pageable)));
    }

    @GetMapping("/api/applications/{id:\\d+}")
    public ResponseEntity<FormResponse> form(@PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.getForm(id));
    }

    @PostMapping("/api/applications/{id:\\d+}/submissions")
    public ResponseEntity<SubmissionResponse> submit(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                                     @RequestBody @Valid final SubmitRequest request) {
        SubmissionResponse response = applicationService.submit(user, id, request);
        return ResponseEntity.created(URI.create("/api/applications/me")).body(response);
    }

    @GetMapping("/api/applications/me")
    public ResponseEntity<PagedModel<SubmissionResponse>> mine(@AuthenticationPrincipal final LoginUser user,
                                                               @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(applicationService.getMine(user.id(), pageable)));
    }

    @PostMapping("/api/applications/submissions/{id}/withdraw")
    public ResponseEntity<SubmissionResponse> withdraw(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.withdrawMine(user.id(), id));
    }

    @GetMapping("/api/admin/applications")
    public ResponseEntity<PagedModel<FormResponse>> adminForms(@RequestParam(required = false) final String keyword,
                                                               @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(applicationService.getForms(keyword, pageable)));
    }

    @GetMapping("/api/admin/applications/{id}")
    public ResponseEntity<FormResponse> adminForm(@PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.getForm(id));
    }

    @PostMapping("/api/admin/applications")
    public ResponseEntity<Void> create(@RequestBody @Valid final FormSaveRequest request) {
        return ResponseEntity.created(URI.create("/api/applications/" + applicationService.createForm(request))).build();
    }

    @PutMapping("/api/admin/applications/{id}")
    public ResponseEntity<Void> update(@PathVariable final Long id, @RequestBody @Valid final FormSaveRequest request) {
        applicationService.updateForm(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/applications/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        applicationService.deleteForm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/applications/{id}/submissions")
    public ResponseEntity<PagedModel<SubmissionResponse>> submissions(@PathVariable final Long id,
                                                                      @RequestParam(required = false) final SubmissionStatus status,
                                                                      @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(applicationService.getSubmissions(id, status, pageable)));
    }

    @PutMapping("/api/admin/applications/submissions/{id}")
    public ResponseEntity<SubmissionResponse> review(@PathVariable final Long id, @RequestBody @Valid final ReviewRequest request) {
        return ResponseEntity.ok(applicationService.review(id, request));
    }
}
