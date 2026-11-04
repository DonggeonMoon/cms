package com.dgmoonlabs.cms.domain.common.coupon.controller;

import com.dgmoonlabs.cms.domain.common.coupon.dto.CouponDtos.*;
import com.dgmoonlabs.cms.domain.common.coupon.service.CouponService;
import com.dgmoonlabs.cms.global.security.CurrentUser;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/api/coupons")
    public ResponseEntity<List<DownloadableCoupon>> downloadable() {
        Long memberId = CurrentUser.isMember() ? CurrentUser.get().map(LoginUser::id).orElse(null) : null;
        return ResponseEntity.ok(couponService.downloadable(memberId));
    }

    @PostMapping("/api/coupons/{id}/download")
    public ResponseEntity<CouponIssueResponse> download(@AuthenticationPrincipal final LoginUser user, @PathVariable final Long id) {
        return ResponseEntity.ok(couponService.download(user.id(), id));
    }

    @GetMapping("/api/members/me/coupons")
    public ResponseEntity<List<CouponIssueResponse>> mine(@AuthenticationPrincipal final LoginUser user) {
        return ResponseEntity.ok(couponService.mine(user.id()));
    }

    @GetMapping("/api/admin/coupons")
    public ResponseEntity<List<CouponResponse>> coupons() {
        return ResponseEntity.ok(couponService.getCoupons());
    }

    @PostMapping("/api/admin/coupons")
    public ResponseEntity<CouponResponse> create(@RequestBody @Valid final CouponSaveRequest request) {
        return ResponseEntity.ok(couponService.create(request));
    }

    @PutMapping("/api/admin/coupons/{id}")
    public ResponseEntity<CouponResponse> update(@PathVariable final Long id, @RequestBody @Valid final CouponSaveRequest request) {
        return ResponseEntity.ok(couponService.update(id, request));
    }

    @DeleteMapping("/api/admin/coupons/{id}")
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/coupons/{id}/issues")
    public ResponseEntity<PagedModel<CouponIssueResponse>> issues(@PathVariable final Long id,
                                                                  @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(couponService.issuesOf(id, pageable)));
    }

    @PostMapping("/api/admin/coupons/{id}/issues")
    public ResponseEntity<IssueResult> issue(@PathVariable final Long id, @RequestBody @Valid final IssueRequest request) {
        return ResponseEntity.ok(couponService.issueByAdmin(id, request));
    }
}
