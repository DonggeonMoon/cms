package com.dgmoonlabs.cms.domain.common.user.controller;

import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinSettingDtos.AdminMemberJoinSettingResponse;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinSettingDtos.MemberJoinSettingRequest;
import com.dgmoonlabs.cms.domain.common.user.dto.MemberJoinSettingDtos.MemberJoinSettingResponse;
import com.dgmoonlabs.cms.domain.common.user.service.MemberJoinSettingService;
import com.dgmoonlabs.cms.global.security.RequiresReauthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberJoinSettingController {
    private final MemberJoinSettingService service;

    @GetMapping("/api/members/join-settings")
    public ResponseEntity<MemberJoinSettingResponse> publicSetting() {
        return ResponseEntity.ok(service.get());
    }

    @RequiresReauthentication
    @GetMapping("/api/admin/members/join-settings")
    public ResponseEntity<AdminMemberJoinSettingResponse> adminSetting() {
        return ResponseEntity.ok(service.getForAdmin());
    }

    @RequiresReauthentication
    @PutMapping("/api/admin/members/join-settings")
    public ResponseEntity<AdminMemberJoinSettingResponse> update(@RequestBody @Valid final MemberJoinSettingRequest request) {
        return ResponseEntity.ok(service.update(request));
    }
}
