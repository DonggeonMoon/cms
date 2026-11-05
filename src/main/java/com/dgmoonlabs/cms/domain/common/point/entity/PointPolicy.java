package com.dgmoonlabs.cms.domain.common.point.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PointPolicy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("포인트 사용 여부")
    private boolean isEnabled = true;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    @ColumnDefault("1")
    @Comment("구매 적립률(%) - 할인·포인트를 뺀 상품 금액 기준")
    private BigDecimal earnRate = BigDecimal.ONE;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("365")
    @Comment("적립 포인트 유효 일수")
    private int expireDays = 365;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("한 번에 쓸 수 있는 최소 포인트")
    private long minUsePoints = 0;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("100")
    @Comment("주문 상품 금액 중 포인트로 낼 수 있는 최대 비율(%)")
    private int maxUseRate = 100;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("가입 적립 포인트")
    private long signupPoints = 0;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("0")
    @Comment("추천인 적립 포인트 (추천받은 회원이 첫 결제를 마치면 추천인에게 지급)")
    private long referralPoints = 0;

    public void update(final boolean isEnabled, final BigDecimal earnRate, final int expireDays, final long minUsePoints,
                       final int maxUseRate, final long signupPoints, final long referralPoints) {
        this.isEnabled = isEnabled;
        this.earnRate = earnRate;
        this.expireDays = expireDays;
        this.minUsePoints = minUsePoints;
        this.maxUseRate = maxUseRate;
        this.signupPoints = signupPoints;
        this.referralPoints = referralPoints;
    }
}
