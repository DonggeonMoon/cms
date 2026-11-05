package com.dgmoonlabs.cms.domain.business.accounting.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import com.dgmoonlabs.cms.global.error.BusinessException;
import com.dgmoonlabs.cms.global.error.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/** 외상(선출고 후결제) 미수금. 결제·주문 상태와는 별개로, 나중에 받을 돈을 거래처(회원)별로 기록만 해 둔다. */
@Entity
@Table(indexes = @Index(name = "idx_receivable_member", columnList = "memberId"))
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Receivable extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("거래처(회원) ID")
    private Long memberId;

    @Comment("관련 주문 ID")
    private Long orderId;

    @Column(nullable = false, precision = 15, scale = 2)
    @Comment("외상 총액")
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    @Comment("수금액 (누적)")
    private BigDecimal collectedAmount = BigDecimal.ZERO;

    @Column(length = 200)
    @Comment("메모")
    private String note;

    public BigDecimal remaining() {
        return totalAmount.subtract(collectedAmount);
    }

    public boolean isSettled() {
        return remaining().signum() <= 0;
    }

    public void collect(final BigDecimal amount) {
        if (amount.signum() <= 0 || amount.compareTo(remaining()) > 0) {
            throw new BusinessException(ErrorCode.RECEIVABLE_OVERPAID);
        }
        this.collectedAmount = this.collectedAmount.add(amount);
    }
}
