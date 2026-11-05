package com.dgmoonlabs.cms.domain.business.currency.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/** 다통화 표시용 환율. 실제 결제는 항상 원화로 이뤄지고, 상품가를 안내용으로 환산해서 보여줄 때만 쓴다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ExchangeRate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    @Comment("통화 코드 (ISO 4217, 예: USD)")
    private String currencyCode;

    @Column(nullable = false, precision = 15, scale = 4)
    @Comment("1 단위당 원화 금액 (예: USD 1350.00)")
    private BigDecimal rate;

    @Column(nullable = false)
    @Comment("표시 사용 여부")
    private boolean enabled;

    public void update(final BigDecimal rate, final boolean enabled) {
        this.rate = rate;
        this.enabled = enabled;
    }
}
