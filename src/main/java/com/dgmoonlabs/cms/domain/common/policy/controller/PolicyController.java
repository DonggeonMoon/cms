package com.dgmoonlabs.cms.domain.common.policy.controller;

import com.dgmoonlabs.cms.domain.common.policy.constant.PolicyType;
import com.dgmoonlabs.cms.domain.common.policy.dto.PolicyDtos.PolicyResponse;
import com.dgmoonlabs.cms.domain.common.policy.dto.PolicyDtos.PolicySaveRequest;
import com.dgmoonlabs.cms.domain.common.policy.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @GetMapping({"/api/policies/{type}", "/api/admin/policies/{type}"})
    public ResponseEntity<PolicyResponse> get(@PathVariable final PolicyType type) {
        return ResponseEntity.ok(policyService.getPolicy(type));
    }

    @PutMapping("/api/admin/policies/{type}")
    public ResponseEntity<PolicyResponse> save(@PathVariable final PolicyType type,
                                               @RequestBody @Valid final PolicySaveRequest request) {
        return ResponseEntity.ok(policyService.savePolicy(type, request));
    }
}
