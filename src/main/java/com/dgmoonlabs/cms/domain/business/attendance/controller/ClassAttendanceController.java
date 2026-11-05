package com.dgmoonlabs.cms.domain.business.attendance.controller;

import com.dgmoonlabs.cms.domain.business.attendance.dto.ClassAttendanceDtos.MarkRequest;
import com.dgmoonlabs.cms.domain.business.attendance.dto.ClassAttendanceDtos.MyAttendance;
import com.dgmoonlabs.cms.domain.business.attendance.dto.ClassAttendanceDtos.RosterEntry;
import com.dgmoonlabs.cms.domain.business.attendance.service.ClassAttendanceService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClassAttendanceController {
    private final ClassAttendanceService classAttendanceService;

    @GetMapping("/api/admin/class-attendance/roster")
    public ResponseEntity<List<RosterEntry>> roster(@RequestParam final Long productId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date) {
        return ResponseEntity.ok(classAttendanceService.getRoster(productId, date));
    }

    @PostMapping("/api/admin/class-attendance/mark")
    public ResponseEntity<RosterEntry> mark(@RequestBody @Valid final MarkRequest request) {
        return ResponseEntity.ok(classAttendanceService.mark(request));
    }

    @GetMapping("/api/class-attendance/me")
    public ResponseEntity<PagedModel<MyAttendance>> mine(@AuthenticationPrincipal final LoginUser user,
                                                          @RequestParam final Long subscriptionId,
                                                          @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(classAttendanceService.getMine(user.id(), subscriptionId, pageable)));
    }
}
