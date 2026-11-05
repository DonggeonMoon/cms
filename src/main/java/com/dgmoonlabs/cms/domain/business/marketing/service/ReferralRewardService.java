package com.dgmoonlabs.cms.domain.business.marketing.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.common.point.service.PointService;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.entity.member.Member;
import com.dgmoonlabs.cms.domain.common.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 추천받아 가입한 회원이 처음 결제를 마치면 추천인에게 포인트를 지급한다(회원당 한 번만). */
@Service
@RequiredArgsConstructor
public class ReferralRewardService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final PointService pointService;

    @EventListener
    @Transactional
    public void grantOnFirstPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER) {
            return;
        }
        Member member = memberRepository.findByIdAndDeletedAtIsNull(order.getUserId()).orElse(null);
        if (member == null || member.getReferredByMemberId() == null || member.isReferralRewardGranted()) {
            return;
        }
        pointService.grantReferral(member.getReferredByMemberId(), "회원 추천 보상 (" + member.getUsername() + ")");
        member.markReferralRewardGranted();
    }
}
