package com.dgmoonlabs.cms.domain.common.grade.service;

import com.dgmoonlabs.cms.domain.business.order.constant.ProductSaleType;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.grade.constant.GradeChangeReason;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GradeGrantListener {
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final GradeService gradeService;

    /** 구독 등급은 다음 결제일 하루 뒤까지 유지해, 새벽 자동 결제 전에 등급이 풀리지 않게 한다. */
    @EventListener
    public void grantOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER || order.getProductId() == null) {
            return;
        }
        List<Long> productIds = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .map(OrderItem::getProductId).distinct().toList();
        for (Long productId : productIds.isEmpty() ? List.of(order.getProductId()) : productIds) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || product.getGrantGradeId() == null) {
                continue;
            }
            boolean subscription = order.getSaleType() == ProductSaleType.SUBSCRIPTION && product.getBillingCycle() != null;
            LocalDateTime expiresAt = subscription
                    ? product.getBillingCycle().next(LocalDate.now()).plusDays(1).atStartOfDay()
                    : product.getGrantGradeDays() == null ? null : LocalDateTime.now().plusDays(product.getGrantGradeDays());
            gradeService.grantByOrder(order.getUserId(), product.getGrantGradeId(), expiresAt,
                    subscription ? GradeChangeReason.SUBSCRIPTION : GradeChangeReason.PURCHASE, order.getId());
        }
    }

    @EventListener
    public void revokeOnRefund(final OrderRefundedEvent event) {
        List<Long> grades = productRepository.findAllById(event.productIds()).stream()
                .map(Product::getGrantGradeId).filter(Objects::nonNull).distinct().toList();
        if (!grades.isEmpty()) {
            gradeService.revokeByOrder(event.memberId(), event.orderId(), grades);
        }
    }
}
