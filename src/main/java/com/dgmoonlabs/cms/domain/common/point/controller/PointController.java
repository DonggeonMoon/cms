package com.dgmoonlabs.cms.domain.common.point.controller;

import com.dgmoonlabs.cms.domain.common.point.dto.PointDtos.*;
import com.dgmoonlabs.cms.domain.common.point.service.PointService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    @GetMapping("/api/points/policy")
    public ResponseEntity<PointPolicyResponse> policy() {
        return ResponseEntity.ok(pointService.getPolicy());
    }

    @GetMapping("/api/members/me/points")
    public ResponseEntity<PointSummary> mySummary(@AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(pointService.summary(user.id()));
    }

    @GetMapping("/api/members/me/points/history")
    public ResponseEntity<PagedModel<PointLedgerResponse>> myHistory(@AuthenticationPrincipal final LoginUser user,
                                                                     @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(pointService.history(user.id(), pageable)));
    }

    @PutMapping("/api/admin/points/policy")
    public ResponseEntity<PointPolicyResponse> updatePolicy(@RequestBody @Valid final PointPolicyRequest request) {
        return ResponseEntity.ok(pointService.updatePolicy(request));
    }

    @GetMapping("/api/admin/members/{memberId}/points")
    public ResponseEntity<PointSummary> summary(@PathVariable final Long memberId) {
        return ResponseEntity.ok(pointService.summary(memberId));
    }

    @GetMapping("/api/admin/members/{memberId}/points/history")
    public ResponseEntity<PagedModel<PointLedgerResponse>> history(@PathVariable final Long memberId,
                                                                   @PageableDefault final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(pointService.history(memberId, pageable)));
    }

    @PostMapping("/api/admin/members/{memberId}/points")
    public ResponseEntity<Void> adjust(@PathVariable final Long memberId, @RequestBody @Valid final PointAdjustRequest request) {
        pointService.adjust(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
