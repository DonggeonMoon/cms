package com.dgmoonlabs.cms.domain.business.payment.dto;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.domain.business.payment.entity.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PaymentDtos {
    private PaymentDtos() {
    }

    @Builder
    public record PaymentRequest(
            @NotNull PaymentTargetType targetType,
            @NotNull Long targetId,
            @NotNull @PositiveOrZero BigDecimal amount,
            @NotNull PaymentMethod method
    ) {
    }

    public record PaymentResponse(Long id, String paymentNumber, PaymentTargetType targetType, Long targetId, Long memberId, BigDecimal amount,
                                  PaymentMethod method, PaymentStatus status, String transactionId,
                                  LocalDateTime approvedAt, LocalDateTime canceledAt) {
        public static PaymentResponse from(final Payment payment) {
            return new PaymentResponse(payment.getId(), payment.getPaymentNumber(), payment.getTargetType(), payment.getTargetId(),
                    payment.getMemberId(), payment.getAmount(), payment.getMethod(), payment.getStatus(),
                    payment.getTransactionId(), payment.getApprovedAt(), payment.getCanceledAt());
        }
    }
}
