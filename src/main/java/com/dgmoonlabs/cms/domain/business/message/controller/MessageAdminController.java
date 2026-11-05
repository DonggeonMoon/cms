package com.dgmoonlabs.cms.domain.business.message.controller;

import com.dgmoonlabs.cms.domain.business.message.dto.MessageDtos.MessageResponse;
import com.dgmoonlabs.cms.domain.business.message.dto.MessageDtos.MessageSaveRequest;
import com.dgmoonlabs.cms.domain.business.message.service.CustomerMessageService;
import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageAdminController {
    private final CustomerMessageService customerMessageService;

    @GetMapping("/api/admin/messages")
    public ResponseEntity<List<MessageResponse>> list(@RequestParam final ProgressTargetType targetType,
                                                       @RequestParam final Long targetId) {
        return ResponseEntity.ok(customerMessageService.list(targetType, targetId));
    }

    @PostMapping("/api/admin/messages")
    public ResponseEntity<MessageResponse> post(@AuthenticationPrincipal final LoginUser admin,
                                                @RequestParam final ProgressTargetType targetType,
                                                @RequestParam final Long targetId,
                                                @RequestBody @Valid final MessageSaveRequest request) {
        return ResponseEntity.ok(customerMessageService.post(targetType, targetId, UserType.ADMIN, admin.id(), admin.name(), request));
    }
}
