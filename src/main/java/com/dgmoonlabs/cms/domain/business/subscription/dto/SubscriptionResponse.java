package com.dgmoonlabs.cms.domain.business.subscription.dto;

import com.dgmoonlabs.cms.domain.business.order.constant.BillingCycle;
import com.dgmoonlabs.cms.domain.business.subscription.constant.SubscriptionStatus;
import com.dgmoonlabs.cms.domain.business.subscription.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(Long id, Long memberId, Long productId, String productName, Long firstOrderId,
                                   BigDecimal price, BillingCycle billingCycle, SubscriptionStatus status,
                                   LocalDate nextBillingDate, LocalDateTime startedAt, LocalDateTime canceledAt,
                                   LocalDate pausedOn, LocalDate resumeOn, Long pendingProductId, String pendingProductName,
                                   BigDecimal pendingPrice, BillingCycle pendingBillingCycle, LocalDate commitmentEndDate,
                                   Integer earlyTerminationFeeRate, BigDecimal earlyTerminationFee) {
    public static SubscriptionResponse from(final Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getMemberId(), subscription.getProductId(),
                subscription.getProductName(), subscription.getFirstOrderId(), subscription.getPrice(),
                subscription.getBillingCycle(), subscription.getStatus(),
                subscription.isActive() ? subscription.getNextBillingDate() : null, subscription.getCreatedAt(),
                subscription.getCanceledAt(), subscription.getPausedOn(), subscription.getResumeOn(),
                subscription.getPendingProductId(), subscription.getPendingProductName(), subscription.getPendingPrice(),
                subscription.getPendingBillingCycle(), subscription.getCommitmentEndDate(), subscription.getEarlyTerminationFeeRate(),
                subscription.isActive() ? subscription.earlyTerminationFee(LocalDate.now()) : BigDecimal.ZERO);
    }
}
