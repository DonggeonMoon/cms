package com.dgmoonlabs.cms.domain.business.message.controller;

import com.dgmoonlabs.cms.domain.business.message.dto.MessageDtos.MessageResponse;
import com.dgmoonlabs.cms.domain.business.message.dto.MessageDtos.MessageSaveRequest;
import com.dgmoonlabs.cms.domain.business.message.service.CustomerMessageService;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationSubmissionRepository;
import com.dgmoonlabs.cms.domain.business.repair.repository.RepairRequestRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageController {
    private final CustomerMessageService customerMessageService;
    private final OrderRepository orderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final ApplicationSubmissionRepository submissionRepository;

    @GetMapping("/api/messages")
    public ResponseEntity<List<MessageResponse>> list(@AuthenticationPrincipal final LoginUser user,
                                                       @RequestParam final ProgressTargetType targetType,
                                                       @RequestParam final Long targetId) {
        requireOwner(user.id(), targetType, targetId);
        return ResponseEntity.ok(customerMessageService.list(targetType, targetId));
    }

    @PostMapping("/api/messages")
    public ResponseEntity<MessageResponse> post(@AuthenticationPrincipal final LoginUser user,
                                                @RequestParam final ProgressTargetType targetType,
                                                @RequestParam final Long targetId,
                                                @RequestBody @Valid final MessageSaveRequest request) {
        requireOwner(user.id(), targetType, targetId);
        return ResponseEntity.ok(customerMessageService.post(targetType, targetId, UserType.MEMBER, user.id(), user.name(), request));
    }

    /** 대상이 늘어나면(AS, 의뢰, 채용) 이 분기만 늘린다. */
    private void requireOwner(final Long memberId, final ProgressTargetType targetType, final Long targetId) {
        switch (targetType) {
            case ORDER -> orderRepository.findByIdAndUserTypeAndUserId(targetId, UserType.MEMBER, memberId)
                    .orElseThrow(BusinessException::notFound);
            case REPAIR -> repairRequestRepository.findByIdAndMemberId(targetId, memberId).orElseThrow(BusinessException::notFound);
            case APPLICATION -> submissionRepository.findByIdAndMemberId(targetId, memberId).orElseThrow(BusinessException::notFound);
        }
    }
}
