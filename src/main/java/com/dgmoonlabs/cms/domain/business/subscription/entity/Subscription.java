package com.dgmoonlabs.cms.domain.business.subscription.entity;

import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.subscription.constant.SubscriptionStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false)
    @Comment("상품 ID")
    private Long productId;

    @Comment("구독 당시 상품명")
    private String productName;

    @Column(nullable = false, unique = true)
    @Comment("구독을 시작한 첫 주문 ID")
    private Long firstOrderId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("회차별 결제 금액")
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("결제 주기")
    private BillingCycle billingCycle;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("구독 상태")
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(nullable = false)
    @Comment("다음 결제일")
    private LocalDate nextBillingDate;

    @Comment("해지 일시")
    private LocalDateTime canceledAt;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public void billed() {
        this.nextBillingDate = billingCycle.next(nextBillingDate);
    }

    public void cancel() {
        if (isActive()) {
            this.status = SubscriptionStatus.CANCELED;
            this.canceledAt = LocalDateTime.now();
        }
    }
}
