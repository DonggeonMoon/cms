package com.dgmoonlabs.cms.domain.business.order.entity;

import com.dgmoonlabs.cms.domain.business.order.constant.OrderItemStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_order_item_order", columnList = "orderId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("주문 ID")
    private Long orderId;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Comment("상품 옵션(조합) ID")
    private Long productOptionId;

    @Column(nullable = false)
    @Comment("주문 당시 상품명")
    private String productName;

    @Comment("주문 당시 옵션명")
    private String optionName;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("단가 (옵션 추가 금액 포함)")
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @Comment("수량")
    private int quantity;

    @Comment("할인 ID")
    private Long discountId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("결제 금액 (할인 적용 후)")
    private BigDecimal amount;

    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("배송이 필요한 상품인지")
    private boolean requiresShipping;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @ColumnDefault("'ORDERED'")
    @Enumerated(EnumType.STRING)
    @Comment("항목 상태")
    private OrderItemStatus status = OrderItemStatus.ORDERED;

    @Column(precision = 15, scale = 2)
    @Comment("이 항목 취소로 환불한 금액")
    private BigDecimal refundedAmount;

    @Comment("취소 일시")
    private LocalDateTime canceledAt;

    public boolean isActive() {
        return status == OrderItemStatus.ORDERED;
    }

    public void cancel(final BigDecimal refundedAmount) {
        if (!isActive()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderItemStatus.CANCELED;
        this.refundedAmount = refundedAmount;
        this.canceledAt = LocalDateTime.now();
    }
}
