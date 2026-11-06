package com.dgmoonlabs.cms.domain.common.notification.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationType;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.common.user.service.MemberJoinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final NotificationSettingService settingService;

    @EventListener
    public void onOrderPaid(final OrderPaidEvent event) {
        if (!settingService.isEnabled(NotificationType.ORDER_PAID)) {
            return;
        }
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER) {
            return;
        }
        notificationService.notifyAllChannels(order.getUserId(), "결제가 완료되었습니다",
                "주문 번호 " + order.getOrderNumber() + "의 결제가 완료되었습니다.", "/orders/" + order.getId());
    }

    @EventListener
    public void onOrderRefunded(final OrderRefundedEvent event) {
        if (!settingService.isEnabled(NotificationType.ORDER_REFUNDED)) {
            return;
        }
        notificationService.notifyAllChannels(event.memberId(), "환불이 처리되었습니다",
                "주문 번호 " + event.orderId() + "의 환불이 처리되었습니다.", "/orders/" + event.orderId());
    }

    @EventListener
    public void onMemberJoined(final MemberJoinedEvent event) {
        if (!settingService.isEnabled(NotificationType.MEMBER_JOINED)) {
            return;
        }
        notificationService.notifyAllChannels(event.memberId(), "가입을 환영합니다", "회원 가입이 완료되었습니다.", null);
    }
}
