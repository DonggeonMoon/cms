package com.dgmoonlabs.cms.domain.media.service;

import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import com.dgmoonlabs.cms.domain.media.entity.Media;
import com.dgmoonlabs.cms.domain.media.entity.MediaAccess;
import com.dgmoonlabs.cms.domain.media.repository.MediaAccessRepository;
import com.dgmoonlabs.cms.domain.media.repository.MediaRepository;
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
public class MediaSalesService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final MediaRepository mediaRepository;
    private final MediaAccessRepository accessRepository;

    public void syncProduct(final Media media) {
        Product product = media.getProductId() == null ? null : productRepository.findById(media.getProductId()).orElse(null);
        if (!media.isPaid()) {
            if (product != null) {
                product.update(product.getName(), product.getDescription(), product.getImageFileId(), product.getPrice(), null,
                        product.getContentHtml(), product.getContentText(), false);
            }
            return;
        }
        boolean onSale = media.isUsing() && media.getDeletedAt() == null;
        if (product == null) {
            product = productRepository.save(Product.builder()
                    .name(media.getTitle())
                    .description("스트리밍 이용권")
                    .imageFileId(media.getThumbnailFileId())
                    .price(media.getPrice())
                    .contentHtml("")
                    .contentText("")
                    .requiresShipping(false)
                    .mediaId(media.getId())
                    .isEnabled(onSale)
                    .build());
            media.linkProduct(product.getId());
        } else {
            product.update(media.getTitle(), "스트리밍 이용권", media.getThumbnailFileId(), media.getPrice(), null, "", "", onSale);
        }
        product.changeGrades(media.getAccessGradeId(), null, null);
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
            if (product.getMediaId() == null || accessRepository.existsByOrderIdAndMediaId(order.getId(), product.getMediaId())) {
                continue;
            }
            Media media = mediaRepository.findById(product.getMediaId()).orElse(null);
            Integer days = media == null ? null : media.getAccessDays();
            accessRepository.save(MediaAccess.builder()
                    .memberId(order.getUserId())
                    .mediaId(product.getMediaId())
                    .orderId(order.getId())
                    .expiresAt(days == null ? null : now.plusDays(days))
                    .build());
        }
    }

    @EventListener
    public void revokeOnRefund(final OrderRefundedEvent event) {
        List<Long> mediaIds = productRepository.findAllById(event.productIds()).stream()
                .map(Product::getMediaId).filter(Objects::nonNull).toList();
        if (!mediaIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            accessRepository.findAllByOrderIdAndMediaIdIn(event.orderId(), mediaIds).forEach(access -> access.revoke(now));
        }
    }
}
