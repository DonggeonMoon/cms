package com.dgmoonlabs.cms.domain.board.service;

import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticle;
import com.dgmoonlabs.cms.domain.board.entity.aritcle.BoardArticleAccess;
import com.dgmoonlabs.cms.domain.board.repository.BoardArticleAccessRepository;
import com.dgmoonlabs.cms.domain.business.order.entity.Order;
import com.dgmoonlabs.cms.domain.business.order.entity.OrderItem;
import com.dgmoonlabs.cms.domain.business.order.entity.product.Product;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderItemRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.OrderRepository;
import com.dgmoonlabs.cms.domain.business.order.repository.ProductRepository;
import com.dgmoonlabs.cms.domain.business.order.service.OrderPaidEvent;
import com.dgmoonlabs.cms.domain.business.order.service.OrderRefundedEvent;
import com.dgmoonlabs.cms.domain.common.user.constant.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 유료 글(페이월)도 스트리밍과 같은 방식으로 숨긴 판매 상품을 하나씩 두고 일반 주문으로 판다.
 * 결제가 끝나면 열람 권한을 주고 주문이 취소되면 회수한다.
 */
@Service
@RequiredArgsConstructor
public class BoardArticleSalesService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final BoardArticleAccessRepository accessRepository;

    public void syncProduct(final BoardArticle article) {
        Product product = article.getProductId() == null ? null : productRepository.findById(article.getProductId()).orElse(null);
        if (!article.isPaid()) {
            if (product != null) {
                product.update(product.getName(), product.getDescription(), product.getImageFileId(), product.getPrice(), null,
                        product.getContentHtml(), product.getContentText(), false);
            }
            return;
        }
        boolean onSale = !article.isHidden() && article.getDeletedAt() == null;
        if (product == null) {
            product = productRepository.save(Product.builder()
                    .name(article.getTitle())
                    .description("게시글 열람권")
                    .price(article.getPrice())
                    .contentHtml("")
                    .contentText("")
                    .requiresShipping(false)
                    .articleId(article.getId())
                    .isEnabled(onSale)
                    .build());
            article.linkProduct(product.getId());
        } else {
            product.update(article.getTitle(), "게시글 열람권", product.getImageFileId(), article.getPrice(), null, "", "", onSale);
        }
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
        for (Product product : productRepository.findAllById(targets)) {
            if (product.getArticleId() == null || accessRepository.existsByOrderIdAndArticleId(order.getId(), product.getArticleId())) {
                continue;
            }
            accessRepository.save(BoardArticleAccess.builder()
                    .memberId(order.getUserId())
                    .articleId(product.getArticleId())
                    .orderId(order.getId())
                    .build());
        }
    }

    @EventListener
    public void revokeOnRefund(final OrderRefundedEvent event) {
        List<Long> articleIds = productRepository.findAllById(event.productIds()).stream()
                .map(Product::getArticleId).filter(Objects::nonNull).toList();
        if (!articleIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            accessRepository.findAllByOrderIdAndArticleIdIn(event.orderId(), articleIds).forEach(access -> access.revoke(now));
        }
    }
}
