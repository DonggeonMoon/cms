package com.dgmoonlabs.cms.domain.application.controller;

import com.dgmoonlabs.cms.domain.application.constant.SubmissionStatus;
import com.dgmoonlabs.cms.domain.application.dto.ApplicationDtos.*;
import com.dgmoonlabs.cms.domain.admin.logging.AuditedRead;
import com.dgmoonlabs.cms.domain.application.service.ApplicationReportService;
import com.dgmoonlabs.cms.domain.application.service.ApplicationService;
import com.dgmoonlabs.cms.domain.business.order.dto.OrderDtos.OrderResponse;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationReportService reportService;

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

    @PostMapping("/api/applications/submissions/{id}/admission-payment")
    public ResponseEntity<OrderResponse> payAdmission(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.payAdmissionFee(user.id(), id));
    }

    @GetMapping("/api/applications/{id:\\d+}/judging")
    public ResponseEntity<PagedModel<JudgeSubmissionResponse>> judging(@AuthenticationPrincipal final LoginUser user,
                                                                       @PathVariable final Long id,
                                                                       @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(applicationService.getJudgeSubmissions(user.id(), id, pageable)));
    }

    @PostMapping("/api/applications/submissions/{id}/score")
    public ResponseEntity<ScoreResponse> score(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id,
                                               @RequestBody @Valid final ScoreRequest request) {
        return ResponseEntity.ok(applicationService.submitScore(user.id(), id, request));
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

    @PostMapping("/api/admin/applications/{id}/draw")
    public ResponseEntity<DrawResponse> draw(@PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.draw(id));
    }

    @PostMapping("/api/admin/applications/{id}/finalize-contest")
    public ResponseEntity<DrawResponse> finalizeContest(@PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.finalizeContest(id));
    }

    @GetMapping("/api/admin/applications/submissions/{id}/scores")
    public ResponseEntity<List<ScoreDetail>> scores(@PathVariable final Long id) {
        return ResponseEntity.ok(applicationService.getScores(id));
    }

    @GetMapping("/api/admin/applications/{id}/submissions")
    public ResponseEntity<PagedModel<SubmissionResponse>> submissions(@PathVariable final Long id,
                                                                      @RequestParam(required = false) final SubmissionStatus status,
                                                                      @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(applicationService.getSubmissions(id, status, pageable)));
    }

    @GetMapping("/api/admin/applications/{id}/summary")
    public ResponseEntity<SummaryResponse> summary(@PathVariable final Long id,
                                                   @RequestParam(required = false) final SubmissionStatus status) {
        return ResponseEntity.ok(reportService.summarize(id, status));
    }

    @AuditedRead
    @GetMapping("/api/admin/applications/{id}/submissions.csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable final Long id, @RequestParam(required = false) final SubmissionStatus status) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("application-%d-submissions.csv".formatted(id), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(reportService.exportCsv(id, status).getBytes(StandardCharsets.UTF_8));
    }

    @PutMapping("/api/admin/applications/submissions/{id}")
    public ResponseEntity<SubmissionResponse> review(@PathVariable final Long id, @RequestBody @Valid final ReviewRequest request) {
        return ResponseEntity.ok(applicationService.review(id, request));
    }
}
