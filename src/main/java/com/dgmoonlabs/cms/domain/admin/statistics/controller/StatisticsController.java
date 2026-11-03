package com.dgmoonlabs.cms.domain.admin.statistics.controller;

import com.dgmoonlabs.cms.domain.admin.statistics.dto.StatisticsDtos.*;
import com.dgmoonlabs.cms.domain.admin.statistics.service.DashboardService;
import com.dgmoonlabs.cms.domain.admin.statistics.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final DashboardService dashboardService;

    @PostMapping("/api/statistics/visits")
    public ResponseEntity<Void> visit(@RequestBody @Valid final VisitRequest request, final HttpServletRequest httpRequest) {
        statisticsService.recordVisit(request, httpRequest.getHeader(HttpHeaders.USER_AGENT), httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(dashboardService.summary());
    }

    @GetMapping("/api/admin/statistics/daily")
    public ResponseEntity<List<DailyRow>> daily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return ResponseEntity.ok(statisticsService.dailyVisits(from, to));
    }

    @GetMapping("/api/admin/statistics/series")
    public ResponseEntity<List<DailyRow>> series(@RequestParam final PeriodUnit unit,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return ResponseEntity.ok(statisticsService.visitSeries(unit, from, to));
    }

    @GetMapping("/api/admin/statistics/breakdown")
    public ResponseEntity<List<CountRow>> breakdown(@RequestParam final Dimension dimension,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate from,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate to) {
        return ResponseEntity.ok(statisticsService.breakdown(dimension, from, to));
    }

    @GetMapping("/api/admin/statistics/visits")
    public ResponseEntity<PagedModel<VisitLogResponse>> visits(@PageableDefault(size = 30) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(statisticsService.visitLogs(pageable)));
    }
}
