package com.dgmoonlabs.cms.domain.common.coupon.entity;

import com.dgmoonlabs.cms.domain.common.coupon.constant.CouponIssueType;
import com.dgmoonlabs.cms.domain.common.coupon.constant.DiscountType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Coupon extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Comment("쿠폰 이름")
    private String name;

    @Column(length = 255)
    @Comment("설명")
    private String description;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("할인 방식 (정액/정률)")
    private DiscountType discountType;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("할인 금액 또는 할인율(%)")
    private BigDecimal discountValue;

    @Column(precision = 15, scale = 2)
    @Comment("정률 할인의 최대 할인 금액")
    private BigDecimal maxDiscountAmount;

    @Column(precision = 15, scale = 2)
    @Comment("최소 주문 금액")
    private BigDecimal minOrderAmount;

    @Comment("적용 상품 ID (없으면 모든 상품)")
    private Long productId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("발급 방식")
    private CouponIssueType issueType;

    @Comment("등급 달성 시 지급할 등급 ID")
    private Long targetGradeId;

    @Comment("발급(다운로드) 시작 일시")
    private LocalDateTime issueStartAt;

    @Comment("발급(다운로드) 종료 일시")
    private LocalDateTime issueEndAt;

    @Comment("발급 후 사용 가능 일수 (없으면 사용 종료 일시까지)")
    private Integer validDays;

    @Comment("사용 종료 일시")
    private LocalDateTime validUntil;

    @Comment("총 발급 수량 (없으면 무제한)")
    private Integer totalQuantity;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("발급된 수량")
    private int issuedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("사용 여부")
    private boolean isEnabled = true;

    public void update(final String name, final String description, final DiscountType discountType,
                       final BigDecimal discountValue, final BigDecimal maxDiscountAmount, final BigDecimal minOrderAmount,
                       final Long productId, final CouponIssueType issueType, final Long targetGradeId,
                       final LocalDateTime issueStartAt, final LocalDateTime issueEndAt, final Integer validDays,
                       final LocalDateTime validUntil, final Integer totalQuantity, final boolean isEnabled) {
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = minOrderAmount;
        this.productId = productId;
        this.issueType = issueType;
        this.targetGradeId = targetGradeId;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.validDays = validDays;
        this.validUntil = validUntil;
        this.totalQuantity = totalQuantity;
        this.isEnabled = isEnabled;
    }

    public boolean isIssuableAt(final LocalDateTime now) {
        return isEnabled && getDeletedAt() == null
                && (issueStartAt == null || !issueStartAt.isAfter(now))
                && (issueEndAt == null || issueEndAt.isAfter(now))
                && (totalQuantity == null || issuedCount < totalQuantity);
    }

    public boolean isSoldOut() {
        return totalQuantity != null && issuedCount >= totalQuantity;
    }

    public void countIssue() {
        issuedCount++;
    }

    public LocalDateTime expiryFrom(final LocalDateTime issuedAt) {
        LocalDateTime byDays = validDays == null ? null : issuedAt.plusDays(validDays);
        if (byDays == null) {
            return validUntil;
        }
        return validUntil == null || byDays.isBefore(validUntil) ? byDays : validUntil;
    }

    public BigDecimal discountFor(final BigDecimal base) {
        if (base.signum() <= 0 || (minOrderAmount != null && base.compareTo(minOrderAmount) < 0)) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = discountType == DiscountType.AMOUNT ? discountValue
                : base.multiply(discountValue).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        if (discountType == DiscountType.RATE && maxDiscountAmount != null) {
            discount = discount.min(maxDiscountAmount);
        }
        return discount.min(base);
    }
}
