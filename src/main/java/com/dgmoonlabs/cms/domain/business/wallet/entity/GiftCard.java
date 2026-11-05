package com.dgmoonlabs.cms.domain.business.wallet.entity;

import com.dgmoonlabs.cms.domain.business.wallet.constant.GiftCardStatus;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 지갑 잔액 일부를 코드로 바꿔 선물할 수 있는 상품권. 받은 사람이 코드를 입력하면 자기 지갑으로 들어간다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class GiftCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    @Comment("상품권 코드")
    private String code;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("금액")
    private BigDecimal amount;

    @Column(nullable = false)
    @Comment("발급(구매)한 회원 ID")
    private Long issuedMemberId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("상태")
    private GiftCardStatus status;

    @Comment("받아서 쓴 회원 ID")
    private Long redeemedMemberId;

    @Comment("쓴 일시")
    private LocalDateTime redeemedAt;

    public void redeem(final Long memberId, final LocalDateTime now) {
        this.redeemedMemberId = memberId;
        this.redeemedAt = now;
        this.status = GiftCardStatus.REDEEMED;
    }
}
