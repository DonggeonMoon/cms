package com.dgmoonlabs.cms.domain.common.point.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueType;
import com.dgmoonlabs.cms.domain.common.coupon.service.CouponService;
import com.dgmoonlabs.cms.domain.common.grade.service.GradeChangedEvent;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.service.MemberJoinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RewardListener {
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final PointService pointService;
    private final CouponService couponService;

    @EventListener
    public void onJoin(final MemberJoinedEvent event) {
        pointService.grantSignup(event.memberId());
        couponService.issueAutomatically(event.memberId(), CouponIssueType.SIGNUP, null);
    }

    @EventListener
    public void onGradeChange(final GradeChangedEvent event) {
        couponService.issueAutomatically(event.memberId(), CouponIssueType.GRADE, event.gradeId());
    }

    /** 실제로 낸 상품 금액(쿠폰·포인트 제외, 배송비 제외)에 적립률을 곱해 항목마다 적립한다. */
    @EventListener
    public void earnOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER || order.getPointsEarned() > 0) {
            return;
        }
        List<OrderItem> items = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .filter(OrderItem::isActive).toList();
        long total = 0;
        for (OrderItem item : items) {
            long earned = pointService.earnFor(item.paidAmount());
            item.recordEarned(earned);
            total += earned;
        }
        order.recordEarned(total);
        pointService.earn(order.getUserId(), total, order.getId());
    }
}
