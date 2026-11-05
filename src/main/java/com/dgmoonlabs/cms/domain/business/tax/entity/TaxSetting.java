package com.dgmoonlabs.cms.domain.business.tax.entity;

import com.dgmoonlabs.cms.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 세금계산서·현금영수증은 다루지 않는다(제외 확정). 부가세는 표시용 안내일 뿐, 상품가·결제금액 자체는 바꾸지 않는다. */
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class TaxSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    @ColumnDefault("10")
    @Comment("부가세율 (%)")
    private BigDecimal vatRate = BigDecimal.TEN;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    @Comment("상품가에 부가세가 포함돼 있는지")
    private boolean priceIncludesVat = true;

    @Column(length = 300)
    @Comment("해외 배송 주문에 보여줄 관세 안내 문구")
    private String customsNotice;

    public void update(final BigDecimal vatRate, final boolean priceIncludesVat, final String customsNotice) {
        this.vatRate = vatRate;
        this.priceIncludesVat = priceIncludesVat;
        this.customsNotice = customsNotice;
    }

    /** 포함가면 상품가에서 역산, 별도가면 그대로 곱해서 안내용 부가세액을 계산한다. */
    public BigDecimal vatAmountOf(final BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = vatRate.divide(BigDecimal.valueOf(100));
        BigDecimal divisor = priceIncludesVat ? BigDecimal.ONE.add(rate) : BigDecimal.ONE;
        return amount.divide(divisor, 0, RoundingMode.HALF_UP).multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }
}
