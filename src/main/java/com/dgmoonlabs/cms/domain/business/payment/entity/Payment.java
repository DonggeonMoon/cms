package com.dgmoonlabs.cms.domain.business.payment.entity;

import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentMethod;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentStatus;
import com.dgmoonlabs.cms.domain.business.payment.constant.PaymentTargetType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(indexes = @Index(name = "idx_payment_target", columnList = "targetType, targetId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 30)
    @Comment("결제번호")
    private String paymentNumber;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("결제 대상 종류")
    private PaymentTargetType targetType;

    @Column(nullable = false)
    @Comment("결제 대상 ID (주문/예약)")
    private Long targetId;

    @Column(nullable = false)
    @Comment("결제 회원 ID")
    private Long memberId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("결제 금액")
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("결제 수단")
    private PaymentMethod method;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Comment("결제 상태")
    private PaymentStatus status;

    @Column(nullable = false, unique = true)
    @Comment("PG 거래 ID (목업)")
    private String transactionId;

    @Comment("승인 일시")
    private LocalDateTime approvedAt;

    @Comment("취소 일시")
    private LocalDateTime canceledAt;

    @Column(precision = 15, scale = 2)
    @Comment("부분 환불 누적 금액")
    private BigDecimal refundedAmount;

    public void assignNumber(final String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public BigDecimal refundable() {
        return refundedAmount == null ? amount : amount.subtract(refundedAmount);
    }

    public void refund(final BigDecimal refund) {
        this.refundedAmount = (refundedAmount == null ? BigDecimal.ZERO : refundedAmount).add(refund);
        if (refundable().signum() <= 0) {
            cancel();
        }
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
