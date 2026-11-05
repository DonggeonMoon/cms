package com.dgmoonlabs.cms.domain.business.marketing.service;

import com.dgmoonlabs.cms.domain.business.marketing.dto.MarketingDtos.CampaignResponse;
import com.dgmoonlabs.cms.domain.business.marketing.dto.MarketingDtos.CampaignSendRequest;
import com.dgmoonlabs.cms.domain.business.marketing.entity.MarketingCampaign;
import com.dgmoonlabs.cms.domain.business.marketing.repository.MarketingCampaignRepository;
import com.dgmoonlabs.cms.domain.common.notification.service.NotificationService;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 등급으로 대상을 좁혀 회원에게 바로 발송한다(예약 발송은 없음). */
@Service
@RequiredArgsConstructor
public class MarketingCampaignService {
    private final MarketingCampaignRepository campaignRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    /** 마케팅 활용 동의가 없는 회원은 발송 대상에서 제외된다. */
    @Transactional
    public CampaignResponse send(final CampaignSendRequest request) {
        LocalDateTime now = LocalDateTime.now();
        List<Member> targets = memberRepository.findAllByDeletedAtIsNull().stream()
                .filter(member -> request.targetGradeId() == null || request.targetGradeId().equals(member.currentGradeId(now)))
                .toList();
        long sentCount = targets.stream()
                .filter(member -> notificationService.notifyMarketing(member.getId(), request.title(), request.content(), null, request.channels()))
                .count();
        MarketingCampaign saved = campaignRepository.save(MarketingCampaign.builder()
                .title(request.title())
                .content(request.content())
                .targetGradeId(request.targetGradeId())
                .channels(List.copyOf(request.channels()))
                .recipientCount((int) sentCount)
                .sentAt(now)
                .build());
        return CampaignResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> history(final Pageable pageable) {
        return campaignRepository.findAllByOrderByIdDesc(pageable).map(CampaignResponse::from);
    }
}
