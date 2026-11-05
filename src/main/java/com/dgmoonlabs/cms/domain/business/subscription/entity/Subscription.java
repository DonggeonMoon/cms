package com.dgmoonlabs.cms.domain.business.subscription.entity;

import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.subscription.constant.SubscriptionStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
    @Comment("다음 결제일 (일시정지 중에는 멈춘 날 기준의 결제일)")
    private LocalDate nextBillingDate;

    @Comment("해지 일시")
    private LocalDateTime canceledAt;

    @Comment("일시정지한 날")
    private LocalDate pausedOn;

    @Comment("자동으로 다시 시작할 날 (없으면 직접 재개할 때까지)")
    private LocalDate resumeOn;

    @Comment("다음 결제부터 바꿀 상품 ID")
    private Long pendingProductId;

    @Comment("다음 결제부터 바꿀 상품명")
    private String pendingProductName;

    @Column(precision = 15, scale = 2)
    @Comment("다음 결제부터 바꿀 회차별 금액")
    private BigDecimal pendingPrice;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("다음 결제부터 바꿀 결제 주기")
    private BillingCycle pendingBillingCycle;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public boolean isPaused() {
        return status == SubscriptionStatus.PAUSED;
    }

    public void billed() {
        this.nextBillingDate = billingCycle.next(nextBillingDate);
    }

    public boolean hasPendingChange() {
        return pendingProductId != null;
    }

    public void schedulePlanChange(final Long productId, final String productName, final BigDecimal price,
                                   final BillingCycle billingCycle) {
        if (status == SubscriptionStatus.CANCELED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.pendingProductId = productId;
        this.pendingProductName = productName;
        this.pendingPrice = price;
        this.pendingBillingCycle = billingCycle;
    }

    public void cancelPlanChange() {
        this.pendingProductId = null;
        this.pendingProductName = null;
        this.pendingPrice = null;
        this.pendingBillingCycle = null;
    }

    /** 다음 회차 결제 직전에 부른다. 바꾼 주기는 이번 결제 다음 결제일부터 적용된다. */
    public void applyPlanChange() {
        if (!hasPendingChange()) {
            return;
        }
        this.productId = pendingProductId;
        this.productName = pendingProductName;
        this.price = pendingPrice;
        this.billingCycle = pendingBillingCycle;
        cancelPlanChange();
    }

    public void pause(final LocalDate today, final LocalDate resumeOn) {
        if (!isActive()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (resumeOn != null && !resumeOn.isAfter(today)) {
            throw new BusinessException(ErrorCode.INVALID_RESUME_DATE);
        }
        this.status = SubscriptionStatus.PAUSED;
        this.pausedOn = today;
        this.resumeOn = resumeOn;
    }

    /** 멈췄던 동안은 결제 기간에서 빼고, 남아 있던 날수만큼 뒤로 다음 결제일을 잡는다. */
    public void resume(final LocalDate today) {
        if (!isPaused()) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        long remaining = Math.max(0, ChronoUnit.DAYS.between(pausedOn, nextBillingDate));
        this.nextBillingDate = today.plusDays(remaining);
        this.status = SubscriptionStatus.ACTIVE;
        this.pausedOn = null;
        this.resumeOn = null;
    }

    public void cancel() {
        if (status != SubscriptionStatus.CANCELED) {
            this.status = SubscriptionStatus.CANCELED;
            this.canceledAt = LocalDateTime.now();
            this.pausedOn = null;
            this.resumeOn = null;
            cancelPlanChange();
        }
    }
}
