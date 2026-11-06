package com.dgmoonlabs.cms.domain.business.pass.controller;

import com.dgmoonlabs.cms.domain.business.pass.dto.PassDtos.PassResponse;
import com.dgmoonlabs.cms.domain.business.pass.service.PassService;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PassController {
    private final PassService passService;

    @GetMapping("/api/passes/me")
    public ResponseEntity<PagedModel<PassResponse>> mine(@AuthenticationPrincipal final LoginUser user,
                                                         @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(passService.getMine(user.id(), pageable)));
    }

    @GetMapping("/api/passes/usable")
    public ResponseEntity<List<PassResponse>> usable(@AuthenticationPrincipal final LoginUser user,
                                                      @RequestParam final Long reservationItemId) {
        return ResponseEntity.ok(passService.usableFor(user.id(), reservationItemId));
    }

    @GetMapping("/api/admin/passes")
    public ResponseEntity<PagedModel<PassResponse>> search(@RequestParam(required = false) final Long memberId,
                                                           @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(passService.search(memberId, pageable)));
    }

    @DeleteMapping("/api/admin/passes/{id}")
    public ResponseEntity<Void> cancel(@PathVariable final Long id) {
        passService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
