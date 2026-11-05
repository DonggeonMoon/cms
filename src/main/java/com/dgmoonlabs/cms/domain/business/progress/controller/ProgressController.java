package com.dgmoonlabs.cms.domain.business.progress.controller;

import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.progress.constant.ProgressTargetType;
import com.dgmoonlabs.cms.domain.business.progress.dto.ProgressDtos.ProgressResponse;
import com.dgmoonlabs.cms.domain.business.progress.service.ProgressStageService;
import com.dgmoonlabs.cms.domain.application.repository.ApplicationSubmissionRepository;
import com.dgmoonlabs.cms.domain.business.repair.repository.RepairRequestRepository;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProgressController {
    private final ProgressStageService progressStageService;
    private final OrderRepository orderRepository;
    private final RepairRequestRepository repairRequestRepository;
    private final ApplicationSubmissionRepository submissionRepository;

    @GetMapping("/api/progress")
    public ResponseEntity<ProgressResponse> get(@AuthenticationPrincipal final LoginUser user,
                                                @RequestParam final ProgressTargetType targetType,
                                                @RequestParam final Long targetId) {
        requireOwner(user.id(), targetType, targetId);
        return ResponseEntity.ok(progressStageService.get(targetType, targetId));
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
