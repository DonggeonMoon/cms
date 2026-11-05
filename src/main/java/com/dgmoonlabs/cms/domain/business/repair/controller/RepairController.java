package com.dgmoonlabs.cms.domain.business.repair.controller;

import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.RepairResponse;
import com.dgmoonlabs.cms.domain.business.repair.dto.RepairDtos.RepairSaveRequest;
import com.dgmoonlabs.cms.domain.business.repair.service.RepairRequestService;
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
public class RepairController {
    private final RepairRequestService repairRequestService;

    @PostMapping("/api/repairs")
    public ResponseEntity<RepairResponse> create(@AuthenticationPrincipal final LoginUser user,
                                                 @RequestBody @Valid final RepairSaveRequest request) {
        return ResponseEntity.ok(repairRequestService.create(user.id(), request));
    }

    @GetMapping("/api/repairs/me")
    public ResponseEntity<PagedModel<RepairResponse>> mine(@AuthenticationPrincipal final LoginUser user,
                                                           @PageableDefault(size = 20) final Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(repairRequestService.getMine(user.id(), pageable)));
    }
}
