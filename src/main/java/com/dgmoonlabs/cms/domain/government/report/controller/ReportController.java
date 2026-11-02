package com.dgmoonlabs.cms.domain.government.report.controller;

import com.dgmoonlabs.cms.domain.government.report.constant.ReportStatus;
import com.dgmoonlabs.cms.domain.government.report.dto.ReportDtos.*;
import com.dgmoonlabs.cms.domain.government.report.service.ReportService;
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
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/api/reports")
    public ResponseEntity<Map<String, Long>> create(@RequestBody @Valid final ReportCreateRequest request) {
        Long id = reportService.createReport(request);
        return ResponseEntity.created(URI.create("/api/reports/" + id)).body(Map.of("id", id));
    }

    @GetMapping("/api/reports/me")
    public ResponseEntity<PagedModel<ReportSummary>> mine(@AuthenticationPrincipal final LoginUser user,
                                                          @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reportService.getMyReports(user.id(), pageable)));
    }

    @GetMapping("/api/reports/{id}")
    public ResponseEntity<ReportDetail> get(@PathVariable final Long id) {
        return ResponseEntity.ok(reportService.getReport(id, null));
    }

    @PostMapping("/api/reports/{id}/access")
    public ResponseEntity<ReportDetail> access(@PathVariable final Long id, @RequestBody final ReportAccessRequest request) {
        return ResponseEntity.ok(reportService.getReport(id, request.password()));
    }

    @GetMapping("/api/admin/reports")
    public ResponseEntity<PagedModel<ReportSummary>> search(@RequestParam(required = false) final ReportStatus status,
                                                            @RequestParam(required = false) final String keyword,
                                                            @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reportService.searchReports(status, keyword, pageable)));
    }

    @PutMapping("/api/admin/reports/{id}/status")
    public ResponseEntity<Void> status(@PathVariable final Long id, @RequestBody @Valid final ReportStatusRequest request) {
        reportService.changeStatus(id, request.status());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/reports/{id}/answers")
    public ResponseEntity<Void> answer(@PathVariable final Long id, @RequestBody @Valid final ReportAnswerRequest request) {
        Long answerId = reportService.answer(id, request);
        return ResponseEntity.created(URI.create("/api/reports/" + id + "/answers/" + answerId)).build();
    }

    @DeleteMapping("/api/admin/reports/{id}/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable final Long id, @PathVariable final Long answerId) {
        reportService.deleteAnswer(id, answerId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/admin/reports/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
