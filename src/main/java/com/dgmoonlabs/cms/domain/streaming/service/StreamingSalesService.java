package com.dgmoonlabs.cms.domain.streaming.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.streaming.entity.Streaming;
import com.dgmoonlabs.cms.domain.streaming.entity.StreamingAccess;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingAccessRepository;
import com.dgmoonlabs.cms.domain.streaming.repository.StreamingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 유료 스트리밍은 숨긴 판매 상품을 하나씩 두고 일반 주문으로 판다. 그래서 쿠폰·포인트·할인·결제를 그대로 쓰고,
 * 결제가 끝나면 시청 권한을 주고 주문이 취소되면 회수한다.
 */
@Service
@RequiredArgsConstructor
public class StreamingSalesService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final StreamingRepository streamingRepository;
    private final StreamingAccessRepository accessRepository;

    public void syncProduct(final Streaming streaming) {
        Product product = streaming.getProductId() == null ? null : productRepository.findById(streaming.getProductId()).orElse(null);
        if (!streaming.isPaid()) {
            if (product != null) {
                product.update(product.getName(), product.getDescription(), product.getImageFileId(), product.getPrice(), null,
                        product.getContentHtml(), product.getContentText(), false);
            }
            return;
        }
        boolean onSale = streaming.isUsing() && streaming.getDeletedAt() == null;
        if (product == null) {
            product = productRepository.save(Product.builder()
                    .name(streaming.getTitle())
                    .description("스트리밍 이용권")
                    .imageFileId(streaming.getThumbnailFileId())
                    .price(streaming.getPrice())
                    .contentHtml("")
                    .contentText("")
                    .requiresShipping(false)
                    .streamingId(streaming.getId())
                    .isEnabled(onSale)
                    .build());
            streaming.linkProduct(product.getId());
        } else {
            product.update(streaming.getTitle(), "스트리밍 이용권", streaming.getThumbnailFileId(), streaming.getPrice(), null, "", "", onSale);
        }
        product.changeGrades(streaming.getAccessGradeId(), null, null);
    }

    @EventListener
    public void grantOnPayment(final OrderPaidEvent event) {
        Order order = orderRepository.findById(event.orderId()).orElse(null);
        if (order == null || order.getUserType() != UserType.MEMBER) {
            return;
        }
        List<Long> productIds = itemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                .filter(OrderItem::isActive).map(OrderItem::getProductId).distinct().toList();
        List<Long> targets = productIds.isEmpty() && order.getProductId() != null ? List.of(order.getProductId()) : productIds;
        LocalDateTime now = LocalDateTime.now();
        for (Product product : productRepository.findAllById(targets)) {
            if (product.getStreamingId() == null || accessRepository.existsByOrderIdAndStreamingId(order.getId(), product.getStreamingId())) {
                continue;
            }
            Streaming streaming = streamingRepository.findById(product.getStreamingId()).orElse(null);
            Integer days = streaming == null ? null : streaming.getAccessDays();
            accessRepository.save(StreamingAccess.builder()
                    .memberId(order.getUserId())
                    .streamingId(product.getStreamingId())
                    .orderId(order.getId())
                    .expiresAt(days == null ? null : now.plusDays(days))
                    .build());
        }
    }

    @EventListener
    public void revokeOnRefund(final OrderRefundedEvent event) {
        List<Long> streamingIds = productRepository.findAllById(event.productIds()).stream()
                .map(Product::getStreamingId).filter(Objects::nonNull).toList();
        if (!streamingIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            accessRepository.findAllByOrderIdAndStreamingIdIn(event.orderId(), streamingIds).forEach(access -> access.revoke(now));
        }
    }
}
