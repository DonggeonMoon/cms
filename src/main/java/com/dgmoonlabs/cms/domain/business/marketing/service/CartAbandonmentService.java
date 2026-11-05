package com.dgmoonlabs.cms.domain.business.marketing.service;

import com.dgmoonlabs.cms.domain.business.order.entity.CartItem;
import com.dgmoonlabs.cms.domain.business.order.repository.CartItemRepository;
import com.dgmoonlabs.cms.domain.common.notification.constant.NotificationChannel;
import com.dgmoonlabs.cms.domain.common.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 장바구니에 담아두고 결제하지 않은 회원에게 하루에 한 번만 리마인드 알림을 보낸다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartAbandonmentService {
    private final CartItemRepository cartItemRepository;
    private final NotificationService notificationService;

    @Value("${cms.cart.abandonment-hours:24}")
    private int abandonmentHours;

    @Scheduled(cron = "${cms.cart.abandonment-cron:0 0 10 * * *}")
    public void remindOnSchedule() {
        int reminded = remind(LocalDateTime.now());
        if (reminded > 0) {
            log.info("Sent cart abandonment reminders to {} members", reminded);
        }
    }

    @Transactional
    public int remind(final LocalDateTime now) {
        List<CartItem> abandoned = cartItemRepository.findAllByRemindedAtIsNullAndCreatedAtLessThanEqual(
                now.minusHours(abandonmentHours));
        Map<Long, List<CartItem>> byMember = abandoned.stream().collect(Collectors.groupingBy(CartItem::getMemberId));
        for (Map.Entry<Long, List<CartItem>> entry : byMember.entrySet()) {
            notificationService.notifyMarketing(entry.getKey(), "장바구니에 담긴 상품이 있어요",
                    "담아두신 상품이 아직 그대로예요. 지금 확인해 보세요.", "/cart", EnumSet.of(NotificationChannel.IN_APP, NotificationChannel.EMAIL));
            entry.getValue().forEach(item -> item.markReminded(now));
        }
        return byMember.size();
    }
}
