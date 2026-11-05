package com.dgmoonlabs.cms.domain.business.wallet.entity;

import com.dgmoonlabs.cms.domain.business.wallet.constant.LedgerType;
import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/** 선불 충전 지갑 내역. 포인트처럼 만료 개념이 없는 현금처럼 다룬다. 잔액은 항상 합으로 계산한다(이 테이블의 balanceAfter는 조회용 기록일 뿐). */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PrepaidLedger extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("회원 ID")
    private Long memberId;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    @Comment("종류")
    private LedgerType type;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("증감액 (충전·환불은 +, 결제·발급은 -)")
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("처리 후 잔액 (조회용 기록)")
    private BigDecimal balanceAfter;

    @Comment("관련 주문 ID")
    private Long orderId;

    @Column(length = 500)
    @Comment("메모")
    private String memo;
}
